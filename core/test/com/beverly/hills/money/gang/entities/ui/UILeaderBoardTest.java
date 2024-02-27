package com.beverly.hills.money.gang.entities.ui;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

public class UILeaderBoardTest {

    private Runnable youLeadRunnable;
    private Runnable lostLeadRunnable;

    @BeforeEach
    public void setUp() {
        youLeadRunnable = mock(Runnable.class);
        lostLeadRunnable = mock(Runnable.class);
    }


    @Test
    public void testConstructorJustMe() {
        int myPlayerId = 10;

        UILeaderBoard leaderBoard = new UILeaderBoard(myPlayerId,
                List.of(UILeaderBoard.LeaderBoardPlayer.builder()
                        .kills(0).id(myPlayerId).name("my name").build()),
                youLeadRunnable, lostLeadRunnable);

        assertEquals(0, leaderBoard.getMyKills());
        assertEquals(1, leaderBoard.getMyPlace());
        assertEquals("0 KILL", leaderBoard.getMyKillsMessage());
        assertEquals("# 1    0 KILL    MY NAME  < YOU\n", leaderBoard.toString());
        verify(youLeadRunnable, never()).run();
        verify(lostLeadRunnable, never()).run();
    }

    @Test
    public void testConstructorManyPlayers() {
        int myPlayerId = 10;
        UILeaderBoard leaderBoard = new UILeaderBoard(myPlayerId,
                List.of(UILeaderBoard.LeaderBoardPlayer
                                .builder()
                                .kills(2).id(myPlayerId).name("my name")
                                .build(),
                        UILeaderBoard.LeaderBoardPlayer
                                .builder()
                                .kills(10).id(999).name("top dog")
                                .build(),
                        UILeaderBoard.LeaderBoardPlayer
                                .builder()
                                .kills(5).id(666).name("other player")
                                .build()),
                youLeadRunnable, lostLeadRunnable);

        assertEquals(2, leaderBoard.getMyKills());
        assertEquals(3, leaderBoard.getMyPlace());
        assertEquals("2 KILLS", leaderBoard.getMyKillsMessage());
        assertEquals(
                "# 1    10 KILLS    TOP DOG\n" +
                        "# 2    5 KILLS    OTHER PLAYER\n" +
                        "# 3    2 KILLS    MY NAME  < YOU\n", leaderBoard.toString());
        verify(youLeadRunnable, never()).run();
        verify(lostLeadRunnable, never()).run();
    }

    @Test
    public void testRegisterKill() {
        int myPlayerId = 10;
        UILeaderBoard leaderBoard = new UILeaderBoard(myPlayerId,
                List.of(UILeaderBoard.LeaderBoardPlayer
                                .builder()
                                .kills(2).id(myPlayerId).name("my name")
                                .build(),
                        UILeaderBoard.LeaderBoardPlayer
                                .builder()
                                .kills(10).id(999).name("top dog")
                                .build(),
                        UILeaderBoard.LeaderBoardPlayer
                                .builder()
                                .kills(5).id(666).name("other player")
                                .build()),
                youLeadRunnable, lostLeadRunnable);

        leaderBoard.registerKill(myPlayerId, 999);


        assertEquals(3, leaderBoard.getMyKills());
        assertEquals(2, leaderBoard.getMyPlace());
        assertEquals("3 KILLS", leaderBoard.getMyKillsMessage());
        assertEquals(
                "# 1    5 KILLS    OTHER PLAYER\n" +
                        "# 2    3 KILLS    MY NAME  < YOU\n", leaderBoard.toString());
        verify(youLeadRunnable, never()).run();
        verify(lostLeadRunnable, never()).run();
    }

    @Test
    public void testRegisterTakingLead() {
        int myPlayerId = 10;
        UILeaderBoard leaderBoard = new UILeaderBoard(myPlayerId,
                List.of(UILeaderBoard.LeaderBoardPlayer
                                .builder()
                                .kills(2).id(myPlayerId).name("my name")
                                .build(),
                        UILeaderBoard.LeaderBoardPlayer
                                .builder()
                                .kills(3).id(999).name("top dog")
                                .build(),
                        UILeaderBoard.LeaderBoardPlayer
                                .builder()
                                .kills(0).id(666).name("other player")
                                .build()),
                youLeadRunnable, lostLeadRunnable);

        leaderBoard.registerKill(myPlayerId, 999);


        assertEquals(3, leaderBoard.getMyKills());
        assertEquals(1, leaderBoard.getMyPlace());
        assertEquals("3 KILLS", leaderBoard.getMyKillsMessage());
        assertEquals(
                "# 1    3 KILLS    MY NAME  < YOU\n" +
                        "# 2    0 KILL    OTHER PLAYER\n", leaderBoard.toString());

        verify(youLeadRunnable).run(); // my player leads
        verify(lostLeadRunnable, never()).run();
    }

    @Test
    public void testRegisterLoosingLead() {
        int myPlayerId = 10;
        UILeaderBoard leaderBoard = new UILeaderBoard(myPlayerId,
                List.of(UILeaderBoard.LeaderBoardPlayer
                                .builder()
                                .kills(4).id(myPlayerId).name("my name")
                                .build(),
                        UILeaderBoard.LeaderBoardPlayer
                                .builder()
                                .kills(3).id(999).name("top dog")
                                .build(),
                        UILeaderBoard.LeaderBoardPlayer
                                .builder()
                                .kills(0).id(666).name("other player")
                                .build(),
                        UILeaderBoard.LeaderBoardPlayer
                                .builder()
                                .kills(0).id(777).name("one more other player")
                                .build()),
                youLeadRunnable, lostLeadRunnable);

        leaderBoard.registerKill(999, 666);
        leaderBoard.registerKill(999, 777);


        assertEquals(4, leaderBoard.getMyKills());
        assertEquals(2, leaderBoard.getMyPlace());
        assertEquals("4 KILLS", leaderBoard.getMyKillsMessage());
        assertEquals(
                "# 1    5 KILLS    TOP DOG\n" +
                        "# 2    4 KILLS    MY NAME  < YOU\n", leaderBoard.toString());

        verify(youLeadRunnable, never()).run();
        verify(lostLeadRunnable).run(); // I have lost lead
    }

    @Test
    public void testAddNewPlayer() {
        int myPlayerId = 10;
        UILeaderBoard leaderBoard = new UILeaderBoard(myPlayerId,
                List.of(UILeaderBoard.LeaderBoardPlayer
                                .builder()
                                .kills(4).id(myPlayerId).name("my name")
                                .build(),
                        UILeaderBoard.LeaderBoardPlayer
                                .builder()
                                .kills(3).id(999).name("top dog")
                                .build(),
                        UILeaderBoard.LeaderBoardPlayer
                                .builder()
                                .kills(2).id(666).name("other player")
                                .build(),
                        UILeaderBoard.LeaderBoardPlayer
                                .builder()
                                .kills(1).id(777).name("one more other player")
                                .build()),
                youLeadRunnable, lostLeadRunnable);

        leaderBoard.addNewPlayer(UILeaderBoard.LeaderBoardPlayer
                .builder()
                .kills(0).id(5555).name("new player")
                .build());


        assertEquals(4, leaderBoard.getMyKills());
        assertEquals(1, leaderBoard.getMyPlace());
        assertEquals("4 KILLS", leaderBoard.getMyKillsMessage());
        assertEquals(
                "# 1    4 KILLS    MY NAME  < YOU\n" +
                        "# 2    3 KILLS    TOP DOG\n" +
                        "# 3    2 KILLS    OTHER PLAYER\n" +
                        "# 4    1 KILL    ONE MORE OTHER PLAYER\n" +
                        "# 5    0 KILL    NEW PLAYER\n", leaderBoard.toString());

        verify(youLeadRunnable, never()).run();
        verify(lostLeadRunnable, never()).run();
    }

    @Test
    public void testAddNewPlayerTwice() {
        int myPlayerId = 10;
        UILeaderBoard leaderBoard = new UILeaderBoard(myPlayerId,
                List.of(UILeaderBoard.LeaderBoardPlayer
                                .builder()
                                .kills(4).id(myPlayerId).name("my name")
                                .build(),
                        UILeaderBoard.LeaderBoardPlayer
                                .builder()
                                .kills(3).id(999).name("top dog")
                                .build(),
                        UILeaderBoard.LeaderBoardPlayer
                                .builder()
                                .kills(2).id(666).name("other player")
                                .build(),
                        UILeaderBoard.LeaderBoardPlayer
                                .builder()
                                .kills(1).id(777).name("one more other player")
                                .build()),
                youLeadRunnable, lostLeadRunnable);

        leaderBoard.addNewPlayer(UILeaderBoard.LeaderBoardPlayer
                .builder()
                .kills(0).id(5555).name("new player")
                .build());

        leaderBoard.addNewPlayer(UILeaderBoard.LeaderBoardPlayer
                .builder()
                .kills(0).id(5555).name("new player")
                .build()); // add second time

        // nothing should change
        assertEquals(4, leaderBoard.getMyKills());
        assertEquals(1, leaderBoard.getMyPlace());
        assertEquals("4 KILLS", leaderBoard.getMyKillsMessage());
        assertEquals(
                "# 1    4 KILLS    MY NAME  < YOU\n" +
                        "# 2    3 KILLS    TOP DOG\n" +
                        "# 3    2 KILLS    OTHER PLAYER\n" +
                        "# 4    1 KILL    ONE MORE OTHER PLAYER\n" +
                        "# 5    0 KILL    NEW PLAYER\n", leaderBoard.toString());

        verify(youLeadRunnable, never()).run();
        verify(lostLeadRunnable, never()).run();
    }

    @Test
    public void testRemovePlayerGettingLead() {
        int myPlayerId = 10;
        UILeaderBoard leaderBoard = new UILeaderBoard(myPlayerId,
                List.of(UILeaderBoard.LeaderBoardPlayer
                                .builder()
                                .kills(4).id(myPlayerId).name("my name")
                                .build(),
                        UILeaderBoard.LeaderBoardPlayer
                                .builder()
                                .kills(5).id(999).name("top dog")
                                .build()),
                youLeadRunnable, lostLeadRunnable);

        leaderBoard.removePlayer(999);

        assertEquals(4, leaderBoard.getMyKills());
        assertEquals(1, leaderBoard.getMyPlace());
        assertEquals("4 KILLS", leaderBoard.getMyKillsMessage());
        assertEquals("# 1    4 KILLS    MY NAME  < YOU\n", leaderBoard.toString());

        verify(youLeadRunnable).run(); // I'm leading now
        verify(lostLeadRunnable, never()).run();
    }

    @Test
    public void testRegisterKillFirstKillWontLooseLead() {
        int myPlayerId = 10;
        UILeaderBoard leaderBoard = new UILeaderBoard(myPlayerId,
                List.of(UILeaderBoard.LeaderBoardPlayer
                                .builder()
                                .kills(0).id(myPlayerId).name("my name")
                                .build(),
                        UILeaderBoard.LeaderBoardPlayer
                                .builder()
                                .kills(0).id(999).name("top dog")
                                .build(),
                        UILeaderBoard.LeaderBoardPlayer
                                .builder()
                                .kills(0).id(777).name("victim")
                                .build()),
                youLeadRunnable, lostLeadRunnable);

        assertEquals(1, leaderBoard.getMyPlace(), "My place should be 1st with no kill for this test to work");

        leaderBoard.registerKill(999, 777);

        assertEquals(0, leaderBoard.getMyKills());
        assertEquals(2, leaderBoard.getMyPlace());
        assertEquals("0 KILL", leaderBoard.getMyKillsMessage());
        assertEquals("# 1    1 KILL    TOP DOG\n" +
                "# 2    0 KILL    MY NAME  < YOU\n", leaderBoard.toString());

        // I don't lead and I don't lose lead
        verify(youLeadRunnable, never()).run();
        verify(lostLeadRunnable, never()).run();
    }


}
