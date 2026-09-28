package com.dtv.dcp.epoch.model.common.validatecoupons;

import java.io.Serializable;
import java.util.List;

public class CouponResponse implements Serializable {
	

    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/** The minimumPurchaseAmount. */
    private int limit;

    /** The offset. */
    private int offset;

    /** The count. */
    private int count;

    /** The total. */
    private int total;

    private List<Coupon> results;

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

	public List<Coupon> getResults() {
		return results;
	}

	public void setResults(List<Coupon> results) {
		this.results = results;
	}

	@Override
	public String toString() {
		return "CouponResponse [limit=" + limit + ", offset=" + offset + ", count=" + count + ", total=" + total
				+ ", results=" + results + "]";
	}
	

}
