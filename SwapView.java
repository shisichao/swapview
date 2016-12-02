/**
 * Created by Administrator on 2016/12/2.
 */
import java.util.*;
import java.math.*;
import java.nio.charset.*;
import java.nio.file.*;
import java.io.*;

public class SwapView {

    public int pid;
    public double size;
    public String comm;

    public SwapView(int p, double s,String c){
        pid = p;
        size =s;
        comm = c;
    }

    public static String filesize(double size){
        String units = "KMGT";
        int unit = -1;
        double left = Math.abs(size);

        while(left >1100 && unit < 3){
            left /= 1024;
            unit++;
        }
        if(unit == -1){
            return String.format("%dB",(int)size);
        }
        else{
            if(size <0) left = -left;
            return String.format("%.1f%cB",left,units.charAt(unit));
        }

    }

    public static SwapView getSwapFor(int pid){
        try{
                String comm = new String(Files.readAllBytes(
                        Paths.get(String.format("/proc/%d/cmdline",pid))),
                        StandardCharsets.UTF_8);
                comm.replace('\0',' ');
                if(comm.charAt(comm.length()-1) == ' ' ){
                    comm = comm.substring(0,comm.length()-1);
                }

                double s = 0.0;
                for (String line: Files.readAllLines(
                                Paths.get(String.format("/proc/%d/smaps",pid)),
                                StandardCharsets.UTF_8)
                    ) {

                        if(line.startsWith("Swap:")){
                                String[] a = line.split(" ");
                                s += Integer.parseInt(a[a.length -2]);
                        }
                }

                return new SwapView(pid,s*1024,comm);
        }catch(Exception e){
            return new SwapView(pid,0,"");
        }
    }

    public static List<SwapView> getSwap(){
        List<SwapView> list = new ArrayList<SwapView>();
        for (File fpid: new File("/proc").listFiles()) {
            try{
                    int pid = Integer.parseInt(fpid.getName());
                    SwapView swapview = getSwapFor(pid);
                    if(swapview.size >0){
                        list.add(swapview);
                    }
            }catch (NumberFormatException e){}
        }

        Collections.sort(list, new Comparator<SwapView>() {
            @Override
            public int compare(SwapView a, SwapView b) {
                return Double.compare(a.size,b.size);
            }
        });
        return list;
    }

    public static void main(String args[]){
        List<SwapView> list = getSwap();
        System.out.printf("%5s %9s %s\n", "PID", "SWAP", "COMMAND");
        double total = 0.0;
        for (SwapView s:list) {
            System.out.printf("%5s %9s %s\n",s.pid,filesize(s.size),s.comm);
            total += s.size;
        }
        System.out.printf("Total: %8s\n",filesize(total));
    }

}
