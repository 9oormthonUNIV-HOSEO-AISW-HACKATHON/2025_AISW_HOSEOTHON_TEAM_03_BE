package org.hackathon.genon.global.support;

public record PageRequest(
        int page
) {
    private static final int PAGE_SIZE = 10;
    private static final int MOVABLE_PAGE_COUNT = 10;

    public PageRequest {
        if (page < 1) {
            throw new IllegalStateException("page는 1 이상이어야 합니다");
        }
    }

    public int pageSize() {
        return PAGE_SIZE;
    }

    public int offset() {
        return (page - 1) * PAGE_SIZE;
    }

    public int limit() {
        return PAGE_SIZE;
    }

    public int movablePageCount() {
        return MOVABLE_PAGE_COUNT;
    }

}