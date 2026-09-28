package com.dtv.dcp.epoch.model.ct.response;

import java.io.Serializable;
import java.util.List;

import com.dtv.dcp.epoch.model.ct.benefit.Benefit;

public class CTBenefitsResponse  implements Serializable {

    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 1L;


    /** The minimumPurchaseAmount. */
    private int limit;

    /** The offset. */
    private int offset;

    /** The count. */
    private int count;

    /** The total. */
    private int total;

    private List<Benefit> benefits;

    public int getLimit() {
        return limit;
    }

    public void setLimit(int limit) {
        this.limit = limit;
    }

    public int getOffset() {
        return offset;
    }

    public void setOffset(int offset) {
        this.offset = offset;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }

    public List<Benefit> getBenefits() {
        return benefits;
    }

    public void setBenefits(List<Benefit> benefits) {
        this.benefits = benefits;
        for (Benefit benefit : benefits)
        {
        	benefit.setDisclosureMessagesByKey();
        }
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("CTBenefitsResponse [limit=");
        builder.append(limit);
        builder.append(", offset=");
        builder.append(offset);
        builder.append(", count=");
        builder.append(count);
        builder.append(", total=");
        builder.append(total);
        builder.append(", benefits=");
        builder.append(benefits);
        builder.append("]");
        return builder.toString();
    }
}
