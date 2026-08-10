package com.unknown.guzhenren;

import static org.junit.jupiter.api.Assertions.assertSame;

import com.unknown.guzhenren.custom.enums.aperture.Rank;
import com.unknown.guzhenren.custom.enums.aperture.Title;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class TitleTest {

    @Test
    @DisplayName("称号 has THREE grades, and 未开窍 is one of them")
    void unawakenedIsItsOwnGrade() {
        assertSame(Title.MORTAL, Title.fromRank(Rank.NONE));
        assertSame(Title.GU_MASTER, Title.fromRank(Rank.ONE));
    }

    @Test
    @DisplayName("一转 through 五转 are all 蛊师")
    void mortalRanksAreGuMaster() {
        assertSame(Title.GU_MASTER, Title.fromRank(Rank.ONE));
        assertSame(Title.GU_MASTER, Title.fromRank(Rank.TWO));
        assertSame(Title.GU_MASTER, Title.fromRank(Rank.THREE));
        assertSame(Title.GU_MASTER, Title.fromRank(Rank.FOUR));
        assertSame(Title.GU_MASTER, Title.fromRank(Rank.FIVE));
    }

    @Test
    @DisplayName("六转 and above are 蛊仙 -- the phase 3 half of the ladder")
    void immortalRanks() {
        assertSame(Title.GU_IMMORTAL, Title.fromRank(Rank.SIX));
        assertSame(Title.GU_IMMORTAL, Title.fromRank(Rank.SEVEN));
        assertSame(Title.GU_IMMORTAL, Title.fromRank(Rank.EIGHT));
        assertSame(Title.GU_IMMORTAL, Title.fromRank(Rank.NINE));
    }

    @Test
    @DisplayName("every rank has a title -- fromRank is total")
    void fromRankIsTotal() {
        for (Rank rank : Rank.values()) {
            assertSame(true, Title.fromRank(rank) != null, rank + " has no title");
        }
    }

    @Test
    @DisplayName("Rank.shift clamps inside the mortal ladder and never reaches NONE or SIX")
    void rankShiftStaysMortal() {
        assertSame(Rank.ONE, Rank.ONE.shift(-1));
        assertSame(Rank.ONE, Rank.NONE.shift(0));
        assertSame(Rank.FIVE, Rank.FIVE.shift(1));
        assertSame(Rank.FIVE, Rank.FOUR.shift(9));
    }
}
