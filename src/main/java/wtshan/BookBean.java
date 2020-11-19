package wtshan;

import com.alibaba.fastjson.JSON;
import lombok.Data;

@Data
public class BookBean {

    /**
     * venueId : 10000005
     * serviceId : 1002
     * fieldType : 9
     * day : 20201115
     * fieldInfo : 4d86af88e9dc483ba7f48ab8a205046f,119da70e07a35f6562722db357a95ce4,2252c41dd0da82fbb73cbc6f71b6ba77,efbb1d460cb319556640ac05b7e52c12
     */

    private String venueId = "10000005";
    private String serviceId = "1002";
    private String fieldType = "9";
    private String day;
    private String fieldInfo;

    public static String parseToBook(SegmentBean segmentBean, String day) {
        BookBean bookBean = new BookBean();
        bookBean.setDay(day);
        bookBean.setFieldInfo(segmentBean.getFieldSegmentID());
        return JSON.toJSONString(bookBean);
    }
}
