package com.bupt.llm_system.controller;

import com.bupt.llm_system.pojo.dto.ResponseMessage;
import com.bupt.llm_system.pojo.dto.ResultCode;
import com.bupt.llm_system.pojo.internal.MultimodalModerationForm;
import com.bupt.llm_system.service.IMultimodalModerationService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

@RestController
@RequestMapping("/api/v1/moderation/multimodal")
public class MultimodalModerationController {

    private final IMultimodalModerationService moderationService;
    private final ObjectMapper mapper = new ObjectMapper();

    public MultimodalModerationController(IMultimodalModerationService moderationService) {
        this.moderationService = moderationService;
    }

    @PostMapping(value = "/check", consumes = "multipart/form-data")
    public Object checkContent(@ModelAttribute MultimodalModerationForm req) {
        try {
            List<String> allImages = new ArrayList<>();
            if (req.getImageUrls() != null) {
                allImages.addAll(req.getImageUrls());
            }

            if (req.getFiles() != null) {
                for (MultipartFile file : req.getFiles()) {
                    if (!file.isEmpty()) {
                        String base64 = compressAndEncodeImage(file);
                        allImages.add(base64);
                    }
                }
            }

            if (Boolean.TRUE.equals(req.getStream())) {
                SseEmitter emitter = new SseEmitter(60000L); // 60 seconds timeout
                moderationService.checkContentStream(req.getText(), allImages, emitter);
                return emitter;
            } else {
                String rawResult = moderationService.checkContent(req.getText(), allImages);
                try {
                    JsonNode node = mapper.readTree(rawResult);
                    return ResponseMessage.success(node);
                } catch (Exception e) {
                    return ResponseMessage.success(rawResult);
                }
            }

        } catch (Exception e) {
            return ResponseMessage.error(ResultCode.MODEL_ERROR, "审核失败: " + e.getMessage());
        }
    }

    private String compressAndEncodeImage(MultipartFile file) throws IOException {
        // Read image
        BufferedImage originalImage = ImageIO.read(file.getInputStream());
        if (originalImage == null) {
            throw new IOException("Unsupported image format");
        }

        // Resize if too large (e.g., max dimension 1024)
        int maxDimension = 1024;
        int width = originalImage.getWidth();
        int height = originalImage.getHeight();

        BufferedImage processedImage = originalImage;
        if (width > maxDimension || height > maxDimension) {
            double scale = Math.min((double) maxDimension / width, (double) maxDimension / height);
            int newWidth = (int) (width * scale);
            int newHeight = (int) (height * scale);
            Image scaled = originalImage.getScaledInstance(newWidth, newHeight, Image.SCALE_SMOOTH);
            processedImage = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_RGB);
            Graphics2D g2d = processedImage.createGraphics();
            g2d.drawImage(scaled, 0, 0, null);
            g2d.dispose();
        }

        // Compress to JPEG
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        // Use simple ImageIO write for now, defaults are usually okay.
        // For more control, we'd need ImageWriter.
        // Let's stick to standard JPEG writing which is usually compressed.
        // To ensure JPEG format (good for photos):
        boolean success = ImageIO.write(processedImage, "jpg", baos);
        if (!success) {
             // Fallback to png if jpg writer not found (unlikely)
             ImageIO.write(processedImage, "png", baos);
        }

        byte[] bytes = baos.toByteArray();
        String base64 = Base64.getEncoder().encodeToString(bytes);
        return "data:image/jpeg;base64," + base64;
    }
}
