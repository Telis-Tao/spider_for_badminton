package wtshan;

public class CheckAliveThread extends Thread {
    private String url;

    public CheckAliveThread(String url) {
        this.url = url;
    }

    public int status = 0;


    @Override
    public void run() {
        while (status == 0) {
            if (BadmintonService.checkAlive(url) != 0) {
                status = -1;
            }
            try {
                //间隔1分钟一次保活
                Thread.sleep(1 * 60 * 1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
