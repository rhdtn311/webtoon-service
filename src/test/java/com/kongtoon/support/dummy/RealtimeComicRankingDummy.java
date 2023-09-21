package com.kongtoon.support.dummy;

import com.kongtoon.domain.comic.model.Comic;
import com.kongtoon.domain.comic.model.RealtimeComicRanking;
import com.kongtoon.domain.comic.model.dto.response.vo.TwoHourSlice;

public class RealtimeComicRankingDummy {
    public static RealtimeComicRanking createRealtimeComicRanking(TwoHourSlice twoHourSlice, int rank, Comic comic) {
        return new RealtimeComicRanking(twoHourSlice.getPrevSliceDate(), twoHourSlice, rank, 1L, comic);
    }

}
