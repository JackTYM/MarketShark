package dev.jacktym.marketshark.util;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class CoflAPIUtil {
    public static JsonObject getCoflPrice(ItemStack stack) {
        String resp = "";
        try {
            URL url = new URL("https://sky.coflnet.com/api/price/nbt");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            connection.setRequestMethod("POST");
            connection.setRequestProperty("User-Agent", "Mozilla/5.0 (ChatTriggers)");
            connection.setRequestProperty("Content-Type", "application/json; utf-8");
            connection.setRequestProperty("Accept", "application/json");
            connection.setDoOutput(true);

            NBTTagCompound compound = new NBTTagCompound();
            NBTTagList tl = new NBTTagList();

            tl.appendTag(stack.serializeNBT());
            compound.setTag("i", tl);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();

            CompressedStreamTools.writeCompressed(compound, baos);

            String nbt = Base64.getEncoder().encodeToString(baos.toByteArray());

            System.out.println(nbt);

            String jsonInputString = "{"
                    + "\"chestName\": \"\","
                    + "\"fullInventoryNbt\": " + "\"" + nbt + "\""
                    + "}";

            try (OutputStream os = connection.getOutputStream()) {
                byte[] input = jsonInputString.getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }

            // Get the response code
            int responseCode = connection.getResponseCode();
            System.out.println("Response Code: " + responseCode);

            try (BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8))) {
                StringBuilder response = new StringBuilder();
                String responseLine;
                while ((responseLine = br.readLine()) != null) {
                    response.append(responseLine.trim());
                }
                // Print the response JSON
                JsonArray items = new JsonParser().parse(response.toString()).getAsJsonArray();
                return items.get(0).getAsJsonObject();
            }

        } catch (Exception e) {
            BugLogger.logError(e);
        }
        return null;
    }

    public static JsonArray getCoflPrices(ItemStack[] stacks) {
        String resp = "";
        try {
            URL url = new URL("https://sky.coflnet.com/api/price/nbt");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            connection.setRequestMethod("POST");
            connection.setRequestProperty("User-Agent", "Mozilla/5.0 (ChatTriggers)");
            connection.setRequestProperty("Content-Type", "application/json; utf-8");
            connection.setRequestProperty("Accept", "application/json");
            connection.setDoOutput(true);

            NBTTagCompound compound = new NBTTagCompound();
            NBTTagList tl = new NBTTagList();

            for (ItemStack stack : stacks) {
                if (stack != null) {
                    tl.appendTag(stack.serializeNBT());
                } else {
                    tl.appendTag(new NBTTagCompound());
                }
            }
            compound.setTag("i", tl);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();

            CompressedStreamTools.writeCompressed(compound, baos);

            String nbt = Base64.getEncoder().encodeToString(baos.toByteArray());

            System.out.println(nbt);

            String jsonInputString = "{"
                    + "\"chestName\": \"\","
                    + "\"fullInventoryNbt\": " + "\"" + nbt + "\""
                    + "}";

            try (OutputStream os = connection.getOutputStream()) {
                byte[] input = jsonInputString.getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }

            // Get the response code
            int responseCode = connection.getResponseCode();
            System.out.println("Response Code: " + responseCode);

            try (BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8))) {
                StringBuilder response = new StringBuilder();
                String responseLine;
                while ((responseLine = br.readLine()) != null) {
                    response.append(responseLine.trim());
                }
                // Print the response JSON
                JsonArray items = new JsonParser().parse(response.toString()).getAsJsonArray();
                return items.getAsJsonArray();
            }

        } catch (Exception e) {
            BugLogger.logError(e);
        }
        return null;
    }
}
