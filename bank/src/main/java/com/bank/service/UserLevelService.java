package com.bank.service;


import com.bank.dto.UserLevelDTO;
import com.bank.model.UserLevel;
import com.bank.repository.UserLevelRepository;

import javax.transaction.Transactional;
import java.util.List;

@Transactional
public class UserLevelService {
    private UserLevelRepository userLevelRepository;

    public UserLevelService(UserLevelRepository userLevelRepository) {
        this.userLevelRepository = userLevelRepository;
    }

    public UserLevel insert(UserLevelDTO userLevelDTO){
        UserLevel userLevel = new UserLevel();
        userLevel.setLevelName(userLevelDTO.getLevelName());
        userLevel.setCardLimit(userLevelDTO.getCardLimit());
        userLevel.setDailyTransferLimit(userLevelDTO.getDailyTransferLimit());
        return userLevelRepository.save(userLevel);
    }

    public UserLevel update(Long id,UserLevelDTO userLevelDTO){
        return userLevelRepository.findById(id)
                .map(existing -> {
                    if(userLevelDTO.getLevelName() != null && !userLevelDTO.getLevelName().isEmpty()){
                        existing.setLevelName(userLevelDTO.getLevelName());
                    }
                    if(userLevelDTO.getCardLimit() != null ){
                        existing.setCardLimit(userLevelDTO.getCardLimit());
                    }
                    if(userLevelDTO.getDailyTransferLimit() != null){
                        existing.setDailyTransferLimit(userLevelDTO.getDailyTransferLimit());
                    }
                    return userLevelRepository.save(existing);
                })
                .orElseThrow(() -> new RuntimeException("Không tìm người đề xuất với id: " + id));
    }

    public void delete(Long id) {
        userLevelRepository.delete(id);
    }

    public List<UserLevel> getAll() {
        return userLevelRepository.findAll();
    }

    public UserLevel getById(Long id) {
        return userLevelRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy UserLevel với id: " + id));
    }
}
