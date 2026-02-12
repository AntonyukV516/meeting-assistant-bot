package com.antonyukV516.mapper;

import com.antonyukV516.dto.UserDto;
import com.antonyukV516.model.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface UserMapper {

    UserDto toDto(User user);

    @Mapping(target = "chatId", ignore = true)
    User toEntity(UserDto dto);
}