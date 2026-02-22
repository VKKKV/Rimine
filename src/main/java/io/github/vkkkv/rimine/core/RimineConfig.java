package io.github.vkkkv.rimine.core;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class RimineConfig {
  private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
  private static RimineConfig instance = new RimineConfig();

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
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public static void save(Path path) {
    try {
      Files.createDirectories(path.getParent());
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
      int alpha = (int) (candidate_box_opacity * 255) << 24;
      return alpha | (color & 0xFFFFFF);
    } catch (NumberFormatException e) {
      return 0xAA000000;
    }
  }
}
