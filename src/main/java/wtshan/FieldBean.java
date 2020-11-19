package wtshan;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;


/**
 * <span class="disabled
 * " price="35" start-time="33" end-time="34" field-num="12"
 * field-segment-id="6cbeaa0d5a65378487e7fafd80a124f7" state="2" field-id="10000026"> </span>
 */
@Data
public class FieldBean {

    public FieldBean(String fieldNum) {
        this.fieldNum = fieldNum;
    }

    String fieldNum;
    List<SegmentBean> segmentBeanList = new ArrayList<>();


    @Override
    public String toString() {
        return "场地[" + fieldNum + "]号码：" + fieldNum + "场地时间：" + getDurations(segmentBeanList);
    }

    private String getDurations(List<SegmentBean> beanList) {
        String result = "[";
        for (SegmentBean bean : beanList) {
            result += BadmintonUtils.convertTime(bean.getStartTime()) + "-" + BadmintonUtils.convertTime(
                    bean.getEndTime()) + "|";
        }

        result = result.substring(0, result.length() - 1) + "]";
        return result;

    }
}
