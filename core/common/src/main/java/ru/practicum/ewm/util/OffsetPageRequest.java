package ru.practicum.ewm.util;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

public final class OffsetPageRequest implements Pageable {

    private final long offset;
    private final int pageSize;
    private final Sort sort;

    public OffsetPageRequest(long offset, int pageSize) {
        this(offset, pageSize, Sort.unsorted());
    }

    public OffsetPageRequest(long offset, int pageSize, Sort sort) {
        if (offset < 0) {
            throw new IllegalArgumentException("Offset must not be negative");
        }
        if (pageSize < 1) {
            throw new IllegalArgumentException("Page size must be greater than zero");
        }

        this.offset = offset;
        this.pageSize = pageSize;
        this.sort = sort == null ? Sort.unsorted() : sort;
    }

    @Override
    public int getPageNumber() {
        return (int) (offset / pageSize);
    }

    @Override
    public int getPageSize() {
        return pageSize;
    }

    @Override
    public long getOffset() {
        return offset;
    }

    @Override
    public Sort getSort() {
        return sort;
    }

    @Override
    public Pageable next() {
        return new OffsetPageRequest(offset + pageSize, pageSize, sort);
    }

    @Override
    public Pageable previousOrFirst() {
        if (!hasPrevious()) {
            return first();
        }
        return new OffsetPageRequest(offset - pageSize, pageSize, sort);
    }

    @Override
    public Pageable first() {
        return new OffsetPageRequest(0, pageSize, sort);
    }

    @Override
    public Pageable withPage(int pageNumber) {
        if (pageNumber < 0) {
            throw new IllegalArgumentException("Page index must not be negative");
        }
        return new OffsetPageRequest((long) pageNumber * pageSize, pageSize, sort);
    }

    @Override
    public boolean hasPrevious() {
        return offset >= pageSize;
    }
}
