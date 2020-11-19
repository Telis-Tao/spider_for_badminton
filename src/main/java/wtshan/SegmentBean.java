package wtshan;

import lombok.Data;
import org.apache.commons.lang3.StringUtils;

/**
 * <span class="disabled
 * " price="35" start-time="33" end-time="34" field-num="12"
 * field-segment-id="6cbeaa0d5a65378487e7fafd80a124f7" state="2" field-id="10000026"> </span>
 */
@Data
public class SegmentBean {
    String fieldID;
    String state;
    String fieldSegmentID;
    String fieldNum;
    String startTime;
    String endTime;
    String price;

    String status = "available";

    public SegmentBean(String fieldID, String state, String fieldSegmentID, String fieldNum, String startTime, String endTime, String price, String status) {
        this.fieldID = fieldID;
        this.state = state;

        this.fieldSegmentID = fieldSegmentID;
        this.fieldNum = fieldNum;
        this.startTime = startTime;
        this.endTime = endTime;
        this.price = price;
        if (!StringUtils.isBlank(status)) {
            this.status = status;
        }
    }

    @Override
    public String toString() {
        return "场地[" + fieldNum + "]" + "场地时间：" + BadmintonUtils.convertTime(
                startTime) + "-" + BadmintonUtils.convertTime(endTime) +
                " 价格：" + price;
    }

    public static void main(String[] args) {
        for (int i = 0; i < 48; i++) {
            System.out.println(i + "-" + BadmintonUtils.convertTime(String.valueOf(i)));
        }
    }
}
