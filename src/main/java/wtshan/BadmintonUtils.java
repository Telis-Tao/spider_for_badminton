package wtshan;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import net.dongliu.requests.RawResponse;
import net.dongliu.requests.RequestBuilder;
import net.dongliu.requests.Requests;
import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@Slf4j
public class BadmintonUtils {
    public static final String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/53.0.2785.116 Safari/537.36 QBCore/4.0.1301.400 QQBrowser/9.0.2524.400 Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/53.0.2875.116 Safari/537.36 NetType/WIFI MicroMessenger/7.0.5 WindowsWechat";
    public static final String REQ_COURT_LIST = "https://wtshan.xports.cn/aisports-weixin/court/10000005/1002/null/%s";
    public static final String REQ_BOOK_COURT = "https://wtshan.xports.cn/aisports-weixin/court/commit";


    //时间转换
    static String convertTime(String time) {
        int input = Integer.valueOf(time);
        if ((input % 2) == 0) {
            return input / 2 + ":00";
        } else {
            return input / 2 + ":30";
        }

    }

    public static int bookCourt(SegmentBean segmentBean, String day) {
        RequestBuilder req = Requests.post(REQ_BOOK_COURT);
        req.headers(getBookHeaders());
        req.body(BookBean.parseToBook(segmentBean, day));
        RawResponse resp = req.send();
        String resultText = resp.readToText();
        JSONObject resultJsonObject = JSON.parseObject(resultText);
        int resultCode = resultJsonObject.getIntValue("error");
        if (resultCode == 0) {
            log.info("预订成功！预订信息：" + resultJsonObject.getJSONObject("trade").getString("tradeDesc"));
        } else {
            log.info("预订失败！预订信息：" + resultText);
        }
        return resultCode;
    }

    /**
     * 检查是否已经预订过
     *
     * @param currentBookArrayTmp
     * @param segmentBean
     * @return
     */
    public static boolean needBook(Set<String> currentBookArrayTmp, SegmentBean segmentBean) {
        for (String bookTime : currentBookArrayTmp) {
            if (StringUtils.equalsIgnoreCase(bookTime, segmentBean.getStartTime())) {
                return true;
            }
        }
        return false;
    }

    static Map<String, String> getCourtListHeaders() {
        Map<String, String> headersMap = new HashMap<>(10);
        headersMap.put("User-Agent", USER_AGENT);
        headersMap.put("Cookie", SpiderMain.COOKIE);
        headersMap.put("Host", "wtshan.xports.cn");
        return headersMap;
    }

    static Map<String, String> getBookHeaders() {
        Map<String, String> headersMap = new HashMap<>(getCourtListHeaders());
        headersMap.put("Content-Type", "application/json");
        return headersMap;
    }
}
