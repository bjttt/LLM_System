// ...existing code...
package com.bupt.llm_system.service.impl;

import com.bupt.llm_system.pojo.dto.ResponseMessage;
import com.bupt.llm_system.pojo.dto.ResultCode;
import com.bupt.llm_system.pojo.internal.HallucinationRequest;
import com.bupt.llm_system.service.IHallucinationService;
import com.bupt.llm_system.service.ILlmService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.stereotype.Service;

@Service
public class HallucinationServiceImpl implements IHallucinationService {

    private final ILlmService llmService;
    private final ObjectMapper mapper = new ObjectMapper();

    public HallucinationServiceImpl(ILlmService llmService) {
        this.llmService = llmService;
    }

    @Override
    public ResponseMessage<Object> detect(HallucinationRequest req) {
        try {
            String text = req.getText();

            // 1. 正常回答
            String normal = llmService.ask(text);

            // 2. 抑制幻觉的回答（在问题前加入严格的指令）
            String suppressPrompt = "请基于下面的问题给出回答，严格避免凭空编造事实。如果无法确认，请明确回答'我不知道'或'无法确认'。\n问题：" + text;
            String suppressed = llmService.ask(suppressPrompt);

            // 3. 让模型对正常回答进行幻觉评分，返回 JSON 格式 {"score": number, "reason": "..."}
            String scorePrompt = "请基于下面的用户问题和模型给出的正常回答，评估正常回答中包含虚构/幻觉信息的程度，给出一个 0 到 100 的分数，0 表示完全捏造，100 表示完全可信。只输出 JSON，格式：{\"score\": 数值, \"reason\": \"原因简述\"}\n\n问题："
                    + text + "\n\n正常回答：" + normal + "\n\n抑制回答：" + suppressed;

            String scoreRaw = llmService.ask(scorePrompt);

            double score = 0.0;
            try {
                JsonNode node = mapper.readTree(scoreRaw);
                if (node.has("score")) {
                    score = node.get("score").asDouble();
                }
            } catch (Exception ignore) {
                // 如果解析失败，尝试从文本中抽取数字
                String digits = scoreRaw.replaceAll("[^0-9.]+", " ").trim();
                if (!digits.isEmpty()) {
                    try {
                        score = Double.parseDouble(digits.split("\\s+")[0]);
                    } catch (Exception e) {
                        score = 0.0;
                    }
                }
            }

            ObjectNode result = mapper.createObjectNode();
            result.put("normal_answer", normal);
            result.put("suppressed_answer", suppressed);
            result.put("hallucination_score", score);
            result.put("raw_score_output", scoreRaw);

            return ResponseMessage.success(result);

        } catch (Exception e) {
            return ResponseMessage.error(ResultCode.MODEL_ERROR, "幻觉检测失败");
        }
    }
}

