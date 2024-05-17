package com.beverly.hills.money.gang.entities.ui;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.util.List;
import java.util.function.Consumer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class UILeaderBoardTest {

  private Runnable youLeadRunnable;
  private Runnable lostLeadRunnable;

  private Consumer<Integer> onFragsLeft;

  @BeforeEach
  public void setUp() {
    youLeadRunnable = mock(Runnable.class);
    lostLeadRunnable = mock(Runnable.class);
    onFragsLeft = mock(Consumer.class);
  }


  @Test
  public void testConstructorJustMe() {
    int myPlayerId = 10;

    UILeaderBoard leaderBoard = new UILeaderBoard(myPlayerId,
        List.of(UILeaderBoard.LeaderBoardPlayer.builder()
            .kills(0).id(myPlayerId).name("my name").build()), 999,
        youLeadRunnable, lostLeadRunnable, onFragsLeft);

    assertEquals(0, leaderBoard.getMyKills());
    assertEquals(1, leaderBoard.getMyPlace());
    assertEquals("0 KILL", leaderBoard.getMyKillsMessage());
    assertEquals("0 DEATH", leaderBoard.getMyDeathsMessage());
    assertEquals("# 1    0 KILL        0 DEATH       MY NAME  < YOU\n", leaderBoard.toString());
    verify(youLeadRunnable, never()).run();
    verify(lostLeadRunnable, never()).run();
  }


  @Test
  public void testConstructorManyPlayers() {
    int myPlayerId = 10;
    UILeaderBoard leaderBoard = new UILeaderBoard(myPlayerId,
        List.of(UILeaderBoard.LeaderBoardPlayer
                .builder()
                .kills(2).id(myPlayerId).name("my name").deaths(10)
                .build(),
            UILeaderBoard.LeaderBoardPlayer
                .builder()
                .kills(10).id(999).name("top dog")
                .build(),
            UILeaderBoard.LeaderBoardPlayer
                .builder()
                .kills(5).id(666).name("other player")
                .build()), 999,
        youLeadRunnable, lostLeadRunnable, onFragsLeft);

    assertEquals(2, leaderBoard.getMyKills());
    assertEquals(3, leaderBoard.getMyPlace());
    assertEquals("2 KILLS", leaderBoard.getMyKillsMessage());
    assertEquals("10 DEATHS", leaderBoard.getMyDeathsMessage());
    assertEquals(
        "# 1    10 KILLS      0 DEATH       TOP DOG\n" +
            "# 2    5 KILLS       0 DEATH       OTHER PLAYER\n" +
            "# 3    2 KILLS       10 DEATHS     MY NAME  < YOU\n", leaderBoard.toString());
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
                .build()), 999,
        youLeadRunnable, lostLeadRunnable, onFragsLeft);

    leaderBoard.registerKill(myPlayerId, 999);

    assertEquals(3, leaderBoard.getMyKills());
    assertEquals(3, leaderBoard.getMyPlace());
    assertEquals("3 KILLS", leaderBoard.getMyKillsMessage());
    assertEquals(
        "# 1    10 KILLS      1 DEATH       TOP DOG\n" +
            "# 2    5 KILLS       0 DEATH       OTHER PLAYER\n" +
            "# 3    3 KILLS       0 DEATH       MY NAME  < YOU\n", leaderBoard.toString());
    verify(youLeadRunnable, never()).run();
    verify(lostLeadRunnable, never()).run();
  }


  @Test
  public void testRegisterKillNotLosingOrTakingLead() {
    int myPlayerId = 10;
    UILeaderBoard leaderBoard = new UILeaderBoard(myPlayerId,
        List.of(UILeaderBoard.LeaderBoardPlayer
                .builder()
                .kills(4).id(999).name("killer man")
                .build(),
            UILeaderBoard.LeaderBoardPlayer
                .builder()
                .kills(0).id(myPlayerId).name("my name")
                .build()), 999,
        youLeadRunnable, lostLeadRunnable, onFragsLeft);

    leaderBoard.registerKill(myPlayerId, 999);

    assertEquals(1, leaderBoard.getMyKills());
    assertEquals(2, leaderBoard.getMyPlace());

    assertEquals(
        "# 1    4 KILLS       1 DEATH       KILLER MAN\n" +
            "# 2    1 KILL        0 DEATH       MY NAME  < YOU\n", leaderBoard.toString());

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
                .build()), 999,
        youLeadRunnable, lostLeadRunnable, onFragsLeft);

    leaderBoard.registerKill(myPlayerId, 999);

    assertEquals(3, leaderBoard.getMyKills());
    assertEquals(1, leaderBoard.getMyPlace());
    assertEquals("3 KILLS", leaderBoard.getMyKillsMessage());
    assertEquals(
        "# 1    3 KILLS       0 DEATH       MY NAME  < YOU\n" +
            "# 2    3 KILLS       1 DEATH       TOP DOG\n" +
            "# 3    0 KILL        0 DEATH       OTHER PLAYER\n", leaderBoard.toString());

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
                .kills(1).id(666).name("other player")
                .build(),
            UILeaderBoard.LeaderBoardPlayer
                .builder()
                .kills(0).id(777).name("one more other player")
                .build()), 999,
        youLeadRunnable, lostLeadRunnable, onFragsLeft);

    leaderBoard.registerKill(999, 666);
    leaderBoard.registerKill(999, 777);

    assertEquals(4, leaderBoard.getMyKills());
    assertEquals(2, leaderBoard.getMyPlace());
    assertEquals("4 KILLS", leaderBoard.getMyKillsMessage());
    assertEquals(
        "# 1    5 KILLS       0 DEATH       TOP DOG\n" +
            "# 2    4 KILLS       0 DEATH       MY NAME  < YOU\n" +
            "# 3    1 KILL        1 DEATH       OTHER PLAYER\n" +
            "# 4    0 KILL        1 DEATH       ONE MORE OTHER PLAYER\n", leaderBoard.toString());

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
                .build()), 999,
        youLeadRunnable, lostLeadRunnable, onFragsLeft);

    leaderBoard.addNewPlayer(UILeaderBoard.LeaderBoardPlayer
        .builder()
        .kills(0).id(5555).name("new player")
        .build());

    assertEquals(4, leaderBoard.getMyKills());
    assertEquals(1, leaderBoard.getMyPlace());
    assertEquals("4 KILLS", leaderBoard.getMyKillsMessage());
    assertEquals(
        "# 1    4 KILLS       0 DEATH       MY NAME  < YOU\n" +
            "# 2    3 KILLS       0 DEATH       TOP DOG\n" +
            "# 3    2 KILLS       0 DEATH       OTHER PLAYER\n" +
            "# 4    1 KILL        0 DEATH       ONE MORE OTHER PLAYER\n" +
            "# 5    0 KILL        0 DEATH       NEW PLAYER\n", leaderBoard.toString());

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
                .build()), 999,
        youLeadRunnable, lostLeadRunnable, onFragsLeft);

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
        "# 1    4 KILLS       0 DEATH       MY NAME  < YOU\n" +
            "# 2    3 KILLS       0 DEATH       TOP DOG\n" +
            "# 3    2 KILLS       0 DEATH       OTHER PLAYER\n" +
            "# 4    1 KILL        0 DEATH       ONE MORE OTHER PLAYER\n" +
            "# 5    0 KILL        0 DEATH       NEW PLAYER\n", leaderBoard.toString());

    verify(youLeadRunnable, never()).run();
    verify(lostLeadRunnable, never()).run();
  }

  @Test
  public void testRemovePlayerNotGettingLead() {
    int myPlayerId = 10;
    UILeaderBoard leaderBoard = new UILeaderBoard(myPlayerId,
        List.of(UILeaderBoard.LeaderBoardPlayer
                .builder()
                .kills(4).id(myPlayerId).name("my name")
                .build(),
            UILeaderBoard.LeaderBoardPlayer
                .builder()
                .kills(5).id(999).name("top dog")
                .build()), 999,
        youLeadRunnable, lostLeadRunnable, onFragsLeft);

    leaderBoard.removePlayer(999);

    assertEquals(4, leaderBoard.getMyKills());
    assertEquals(1, leaderBoard.getMyPlace());
    assertEquals("4 KILLS", leaderBoard.getMyKillsMessage());
    assertEquals(
        "# 1    4 KILLS       0 DEATH       MY NAME  < YOU\n",
        leaderBoard.toString());

    verify(youLeadRunnable, never()).run(); // you don't get the lead just because somebody left
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
                .build()), 999,
        youLeadRunnable, lostLeadRunnable, onFragsLeft);

    assertEquals(1, leaderBoard.getMyPlace(),
        "My place should be 1st with no kill for this test to work");

    leaderBoard.registerKill(999, 777);

    assertEquals(0, leaderBoard.getMyKills());
    assertEquals(2, leaderBoard.getMyPlace());
    assertEquals("0 KILL", leaderBoard.getMyKillsMessage());
    assertEquals("# 1    1 KILL        0 DEATH       TOP DOG\n" +
        "# 2    0 KILL        0 DEATH       MY NAME  < YOU\n" +
        "# 3    0 KILL        1 DEATH       VICTIM\n", leaderBoard.toString());

    // I don't lead and I don't lose lead
    verify(youLeadRunnable, never()).run();
    verify(lostLeadRunnable, never()).run();
  }


  @Test
  public void testRegisterKillFiveFragsLeft() {
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
                .kills(0).id(666).name("other player")
                .build()), 6,
        youLeadRunnable, lostLeadRunnable, onFragsLeft);

    leaderBoard.registerKill(myPlayerId, 999);

    verify(onFragsLeft).accept(5);
  }

  @Test
  public void testRegisterKillThreeFragsLeft() {
    int myPlayerId = 10;
    UILeaderBoard leaderBoard = new UILeaderBoard(myPlayerId,
        List.of(UILeaderBoard.LeaderBoardPlayer
                .builder()
                .kills(2).id(myPlayerId).name("my name")
                .build(),
            UILeaderBoard.LeaderBoardPlayer
                .builder()
                .kills(1).id(999).name("top dog")
                .build(),
            UILeaderBoard.LeaderBoardPlayer
                .builder()
                .kills(0).id(666).name("other player")
                .build()), 6,
        youLeadRunnable, lostLeadRunnable, onFragsLeft);

    leaderBoard.registerKill(myPlayerId, 999);

    verify(onFragsLeft).accept(3);
  }

  @Test
  public void testRegisterKillTwoFragsLeft() {
    int myPlayerId = 10;
    UILeaderBoard leaderBoard = new UILeaderBoard(myPlayerId,
        List.of(UILeaderBoard.LeaderBoardPlayer
                .builder()
                .kills(3).id(myPlayerId).name("my name")
                .build(),
            UILeaderBoard.LeaderBoardPlayer
                .builder()
                .kills(1).id(999).name("top dog")
                .build(),
            UILeaderBoard.LeaderBoardPlayer
                .builder()
                .kills(0).id(666).name("other player")
                .build()), 6,
        youLeadRunnable, lostLeadRunnable, onFragsLeft);

    leaderBoard.registerKill(myPlayerId, 999);

    verify(onFragsLeft).accept(2);
  }

  @Test
  public void testRegisterKillOneFragLeft() {
    int myPlayerId = 10;
    UILeaderBoard leaderBoard = new UILeaderBoard(myPlayerId,
        List.of(UILeaderBoard.LeaderBoardPlayer
                .builder()
                .kills(4).id(myPlayerId).name("my name")
                .build(),
            UILeaderBoard.LeaderBoardPlayer
                .builder()
                .kills(1).id(999).name("top dog")
                .build(),
            UILeaderBoard.LeaderBoardPlayer
                .builder()
                .kills(0).id(666).name("other player")
                .build()), 6,
        youLeadRunnable, lostLeadRunnable, onFragsLeft);

    leaderBoard.registerKill(myPlayerId, 999);

    verify(onFragsLeft).accept(1);
  }

  @Test
  public void testRegisterKillOneFragLeftDouble() {
    int myPlayerId = 10;
    UILeaderBoard leaderBoard = new UILeaderBoard(myPlayerId,
        List.of(UILeaderBoard.LeaderBoardPlayer
                .builder()
                .kills(4).id(myPlayerId).name("my name")
                .build(),
            UILeaderBoard.LeaderBoardPlayer
                .builder()
                .kills(1).id(999).name("top dog")
                .build(),
            UILeaderBoard.LeaderBoardPlayer
                .builder()
                .kills(0).id(666).name("other player")
                .build()), 6,
        youLeadRunnable, lostLeadRunnable, onFragsLeft);

    leaderBoard.registerKill(myPlayerId, 999);
    leaderBoard.registerKill(999, myPlayerId); // the number of frags left doesn't change here

    // called only once
    verify(onFragsLeft).accept(1);
  }

  @Test
  public void testRegisterKillOneFragLeftAfterRemove() {
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
                .build()), 6,
        youLeadRunnable, lostLeadRunnable, onFragsLeft);

    leaderBoard.removePlayer(myPlayerId);
    // kill twice. after that, one frag is left
    leaderBoard.registerKill(999, 666);
    leaderBoard.registerKill(999, 666);

    // called only once
    verify(onFragsLeft).accept(anyInt());
    verify(onFragsLeft).accept(1);
  }

  @Test
  public void testRegisterKillNoFragLeft() {
    int myPlayerId = 10;
    UILeaderBoard leaderBoard = new UILeaderBoard(myPlayerId,
        List.of(UILeaderBoard.LeaderBoardPlayer
                .builder()
                .kills(5).id(myPlayerId).name("my name")
                .build(),
            UILeaderBoard.LeaderBoardPlayer
                .builder()
                .kills(1).id(999).name("top dog")
                .build(),
            UILeaderBoard.LeaderBoardPlayer
                .builder()
                .kills(0).id(666).name("other player")
                .build()), 6,
        youLeadRunnable, lostLeadRunnable, onFragsLeft);

    leaderBoard.registerKill(myPlayerId, 999);

    verify(onFragsLeft).accept(0);
  }

}
