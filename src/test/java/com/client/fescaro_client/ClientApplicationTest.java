package com.client.fescaro_client;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;
class ClientApplicationTest {

    ClientApplication clientA;
    ClientApplication clientB;

    @AfterEach
    public void stopClient() throws Exception {
        clientA.stopClient();
        clientB.stopClient();
    }

    @Test
    @DisplayName("클라이언트의 메세지는 1초안에 도착해야한다.")
    public void 클라이언트의_메세지는_1초안에_도착한다() throws IOException {

        //given
        long limitTime = 1000000000; //1초

        clientA = new ClientApplication();
        clientB = new ClientApplication();

        clientA.startClient("127.0.0.1", 3000);
        clientB.startClient("127.0.0.1", 3000);

        //when
        long startTime = System.nanoTime();
        clientB.send("Send Message");

        //then
        System.out.println("clientA send Time = " + startTime);
        System.out.println("clientB receive Time = " + clientA.getReceiveTime());
        System.out.println("Elapsed Time = " + (startTime - clientB.getReceiveTime()));
        assertTrue(startTime - clientB.getReceiveTime() < limitTime);
    }
}