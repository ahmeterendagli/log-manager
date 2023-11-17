package com.globalpbx.logmanager.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.globalpbx.logmanager.dto.LogDto;
import com.globalpbx.logmanager.service.LogService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class LogServiceImpl implements LogService {

    private final String logs = "logs";

    private final RedisTemplate<String, String> redisTemplate;

    public LogServiceImpl(RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public void addToQueue(String data) {
        redisTemplate.opsForList().rightPush(logs, data);
    }

    public String processQueue() {
        return redisTemplate.opsForList().leftPop(logs);
    }

    private static final Logger logger = LogManager.getLogger(LogServiceImpl.class);

    @Override
    public String createLog(List<LogDto> logDtoList) {
        for (LogDto logDto : logDtoList) {
            try {
                ObjectMapper mapper = JsonMapper.builder()
                        .addModule(new JavaTimeModule())
                        .build();
                addToQueue(mapper.writeValueAsString(logDto));
            } catch (JsonProcessingException e) {
                logger.error(e.getMessage());
                throw new RuntimeException(e);
            }
        }
        return "logged successfully!";
    }


    @Scheduled(fixedRate = 10000) // Runs every 10 seconds
    public void processLogsFromQueue() throws JsonProcessingException {
        while (true) {
            String log = processQueue();
            if (log == null) {
                logger.info("queue of logs is empty");
                break;
            }
            ObjectMapper mapper = JsonMapper.builder()
                    .addModule(new JavaTimeModule())
                    .build();
            LogDto storedLog = mapper.readValue(log, LogDto.class);

            String logMessage = storedLog.getLogTime().toString() + " " + storedLog.getType() + " " + storedLog.getProjectName() + ":  " + storedLog.getLog();
            processLog(logMessage, storedLog.getProjectName(), storedLog.getType());
        }
    }

    private void processLog(String logMessage, String projectName,String type) {
        final Logger logger = LogManager.getLogger(projectName);
        switch (type) {
            case "info" -> logger.info(logMessage);
            case "warn" -> logger.warn(logMessage);
            case "trace" -> logger.trace(logMessage);
            case "error" -> logger.error(logMessage);
            case "fatal" -> logger.fatal(logMessage);
            case "debug" -> logger.debug(logMessage);
            default -> {
                logger.error("Unsupported log type: " + type);
                throw new IllegalArgumentException("Unsupported log type: " + type);
            }
        }
    }
}
