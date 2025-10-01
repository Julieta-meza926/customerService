package com.mycompany.clientservice.jms.event;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class ClientChangedEvent  {
    private final Long clientId;
}

