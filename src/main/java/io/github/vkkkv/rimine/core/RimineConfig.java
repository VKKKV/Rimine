package io.github.vkkkv.rimine.core;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class RimineConfig {
  private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
  private static RimineConfig instance = new RimineConfig();
  private static final int DEFAULT_BG_COLOR = 0xAA000000;

  public float candidate_box_opacity = 0.66f;
  public String bg_color = "000000"; // Hex string for easier manual editing
  public int ui_offset_x = 0;
  public int ui_offset_y = 10;

  public static RimineConfig get() {
    return instance;
  }

  public static void load(Path path) {
    if (!Files.exists(path)) {
      save(path);
      return;
    }
    try (var reader = Files.newBufferedReader(path)) {
      instance = GSON.fromJson(reader, RimineConfig.class);
      if (instance == null) instance = new RimineConfig();
    } catch (IOException | RuntimeException e) {
      System.err.println("[Rimine] Failed to load config, using defaults: " + path);
      e.printStackTrace();
      instance = new RimineConfig();
    }
  }

  public static void save(Path path) {
    try {
      Path parent = path.getParent();
      if (parent != null) {
        Files.createDirectories(parent);
      }
      try (var writer = Files.newBufferedWriter(path)) {
        GSON.toJson(instance, writer);
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public int getResolvedBgColor() {
    try {
      int color = Integer.parseInt(bg_color, 16);
      float clampedOpacity = Math.max(0.0f, Math.min(1.0f, candidate_box_opacity));
      int alpha = (int) (clampedOpacity * 255) << 24;
      return alpha | (color & 0xFFFFFF);
    } catch (NumberFormatException e) {
      return DEFAULT_BG_COLOR;
    }
  }
}
