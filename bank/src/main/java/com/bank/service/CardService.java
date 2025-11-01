package com.bank.service;

import com.bank.dto.CardDTO;
import com.bank.dto.res.CardRes;

public interface CardService {
    CardRes create(CardDTO cardDTO, String token);

}
