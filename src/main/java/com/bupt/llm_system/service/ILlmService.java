package com.bupt.llm_system.service;


public interface ILlmService {
    String ask(String question);
    String classifyText(String text);
}
