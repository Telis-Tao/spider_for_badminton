package wtshan;

import lombok.extern.slf4j.Slf4j;
import net.dongliu.requests.RawResponse;
import net.dongliu.requests.RequestBuilder;
import net.dongliu.requests.Requests;
import org.apache.commons.lang3.StringUtils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 *
 */
@Slf4j
public class SpiderMain {

    /**
     * 预订时间段：每个时间段是半个小时，最多能订4个时间段
     * 36-18:00
     * 37-18:30
     * 38-19:00
     * 39-19:30
     * 40-20:00
     * 41-20:30
     * 42-21:00
     * 43-21:30
     * 44-22:00
     * 45-22:30
     * 46-23:00
     * 47-23:30
     */
    private static String sBookTimes = "42,43,44,45";

    static Set<String> currentBookArrayTmp = new HashSet<>(Arrays.asList(SpiderMain.sBookTimes.split(",")));

    /**
     * 预订日期
     */
    static String sBookDay = "20201121";
    /**
     * 关键信息，需要通过fiddler抓取获得
     */
    static final String COOKIE = "JSESSIONID=22F1201F4EFC9CD9F0591572EC20A057; Hm_lvt_e50998163f6aed8809b68891497265f3=1605712122; Hm_lpvt_e50998163f6aed8809b68891497265f3=1605712122";

    public static void main(String[] args) throws InterruptedException {
        /**
         * 开始执行日期，预定日期前两天的凌晨
         */
        int iBookStartDta = Integer.valueOf(sBookDay) - 2;
        String sBookStartDate = iBookStartDta + " 00:00:00";
        SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd HH:mm:ss");
        long beginMs = 0L;
        try {
            Date startDate = format.parse(sBookStartDate);
            beginMs = startDate.getTime();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        CheckAliveThread checkAliveThread = new CheckAliveThread(
                String.format(BadmintonUtils.REQ_COURT_LIST, sBookDay));
        checkAliveThread.start();

        //session值有效，且剩余场地非空
        while (!currentBookArrayTmp.isEmpty() && (checkAliveThread.status == 0)) {
            long nowMs = System.currentTimeMillis();
            long deltaMs = beginMs - nowMs;
            if (deltaMs > 0) {
                log.info("提前两天才能抢，还差[" + deltaMs / 1000 + "]秒，先休息一下[" + deltaMs / 10000 + "]秒");
                if (deltaMs / 10 > 0) {
                    Thread.sleep(deltaMs / 10);
                } else {
                    Thread.sleep(1000);
                }
            } else {
                log.info("到点了，开始抢吧!需要抢的时间段：" + currentBookArrayTmp.toString());
                int result = doSpider();
                if (result == -1) {
                    return;
                }
            }
        }
    }

    private static int doSpider() {
        logStage("五台山羽毛球爬虫现在开始,预订日期：" + sBookDay);
        //获取全量场地及排期
        RequestBuilder req = Requests.get(String.format(BadmintonUtils.REQ_COURT_LIST, sBookDay));
        req.headers(BadmintonUtils.getCourtListHeaders());
        RawResponse resp = req.send();
        List<SegmentBean> fieldList = BadmintonService.getFullSegments(resp);
        if (fieldList.isEmpty()) {
            log.info("爬取全量场地列表异常，请检查session值");
            return -1;
        }
//        log.info(fieldList.toString());
        log.info("已爬取羽毛球场地，场地数量*时间段数量=" + fieldList.size());

        //过滤不可预订场地，并排序
        List<SegmentBean> availableSegments = BadmintonService.getAvailableSegments(fieldList);
        logStage("可预订场地数量：" + availableSegments.size());
//        for (SegmentBean segmentBean : availableSegments) {
//            log.info(segmentBean.toString());
//        }

        //过滤只剩下符合预订时间段的场地，并符合时间多的场地优先放置
        ArrayList<FieldBean> matchingSegments = BadmintonService.getMatchingSegments(availableSegments);

        logStage("符合预订时间段的场地[时间多的场地优先放置]：");
        if (matchingSegments.isEmpty()) {
            log.info("预订时间段的场地已经没有了，很遗憾~");
            return -1;
        }

        for (FieldBean fieldBean : matchingSegments) {
            log.info(fieldBean.toString());
        }
        logStage("开始预订：");
        BadmintonService.bookSegments(matchingSegments);
        return 0;
    }

    private static void logStage(String info) {
        log.info(StringUtils.center(info, 40, "="));
    }
}
