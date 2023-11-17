package com.globalpbx.logmanager.service;

import com.globalpbx.logmanager.dto.LogDto;

import java.util.List;

public interface LogService {

    String createLog(List<LogDto> logDtoList);
}
