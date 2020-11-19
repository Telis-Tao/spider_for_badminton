package wtshan;

import lombok.extern.slf4j.Slf4j;
import net.dongliu.requests.RawResponse;
import net.dongliu.requests.RequestBuilder;
import net.dongliu.requests.Requests;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
public class BadmintonService {
    /**
     * session保活
     *
     * @return 0：正常 ，其他：异常
     */
    static int checkAlive(String url) {
        RequestBuilder req = Requests.get(url);
        req.headers(BadmintonUtils.getCourtListHeaders());
        RawResponse resp = req.send();
        List<SegmentBean> fieldList = BadmintonService.getFullSegments(resp);
        if (fieldList.isEmpty()) {
            log.info("[保活后台]爬取全量场地列表异常，请检查session值");
            return -1;
        }
        log.info("[保活后台]保活正常，全量场地大小：" + fieldList.size());
        return 0;
    }


    static void bookSegments(ArrayList<FieldBean> matchingSegments) {
        for (FieldBean fieldBean : matchingSegments) {
            for (SegmentBean segmentBean : fieldBean.getSegmentBeanList()) {
                if (BadmintonUtils.needBook(SpiderMain.currentBookArrayTmp, segmentBean)) {
                    int bookResult = BadmintonUtils.bookCourt(segmentBean, SpiderMain.sBookDay);
                    if (bookResult == 0) {
                        SpiderMain.currentBookArrayTmp.remove(segmentBean.getStartTime());
                    }
                }
            }
        }
    }

    static ArrayList<FieldBean> getMatchingSegments(List<SegmentBean> availableSegments) {
        List<SegmentBean> suitableSegmentList = availableSegments.stream()
                .filter(segmentBean -> {
                    for (String bookTime : SpiderMain.currentBookArrayTmp) {
                        int bookTimeInt = Integer.valueOf(bookTime);
                        if (bookTimeInt >= Integer.valueOf(
                                segmentBean.getStartTime()) && bookTimeInt <= Integer.valueOf(
                                segmentBean.getEndTime())) {
                            return true;
                        }
                    }
                    return false;
                })
                .collect(Collectors.toList());

        Map<String, FieldBean> fieldBeanMap = new HashMap<>();
        for (SegmentBean segmentBean : suitableSegmentList) {
            if (fieldBeanMap.get(segmentBean.getFieldNum()) == null) {
                fieldBeanMap.put(segmentBean.getFieldNum(), new FieldBean(segmentBean.getFieldNum()));
            }
            fieldBeanMap.get(segmentBean.getFieldNum()).getSegmentBeanList().add(segmentBean);
        }
        ArrayList<FieldBean> suitableFieldList = new ArrayList<>(fieldBeanMap.values());
        suitableFieldList.sort((o1, o2) -> o2.getSegmentBeanList().size() - o1.getSegmentBeanList().size());
        return suitableFieldList;
    }

    static List<SegmentBean> getAvailableSegments(List<SegmentBean> fieldList) {
        return fieldList.stream().filter(
                //过滤无法订场的
                segmentBean -> !StringUtils.equalsIgnoreCase(segmentBean.getStatus(), "disabled"))
                //将场地放在一起
                .sorted((o1, o2) -> {
                    int fieldCompare = Integer.valueOf(o1.getFieldNum()) - Integer.valueOf(o2.getFieldNum());
                    if (fieldCompare == 0) {
                        return Integer.valueOf(o1.getStartTime()) - Integer.valueOf(o2.getStartTime());
                    }
                    return fieldCompare;
                })
                .collect(Collectors.toList());
    }

    /**
     * 获取全量场地
     *
     * @param resp
     * @return
     */
    static List<SegmentBean> getFullSegments(RawResponse resp) {
        Document doc = Jsoup.parse(resp.readToText());
//        获取全量场地
        Elements list = doc.select(".field-list > div > div > span");
        List<SegmentBean> fieldList = new ArrayList<>();
        for (Element element : list) {
            SegmentBean segmentBean = new SegmentBean(element.attr("field-id"), element.attr("state"),
                    element.attr("field-segment-id"), element.attr("field-num"), element.attr("start-time"),
                    element.attr("end-time"), element.attr("price"), StringUtils.trim(element.attr("class")));
            fieldList.add(segmentBean);
        }
        return fieldList;
    }

}
