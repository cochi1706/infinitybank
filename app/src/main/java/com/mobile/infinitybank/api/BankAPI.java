package com.mobile.infinitybank.api;

import android.os.AsyncTask;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

public class BankAPI extends AsyncTask<String, Void, String> {
    private Map<String, String> bankMap = new HashMap<>();
    String api_key = "1bf8f1a0-d49a-4b3d-b61d-b875a940765akey";
    String api_secret = "eeb414fe-65bb-499a-b4e2-718d682e53d5secret";

    public BankAPI() {
        String url = "https://api.banklookup.net/api/bank/list";
        bankMap.put("ACB", "ACB");
        bankMap.put("BIDV", "BIDV");
        bankMap.put("HDBank", "HDB");
        bankMap.put("ViettelMoney", "VTLMONEY");
        bankMap.put("Vietcombank", "VCB");
        bankMap.put("Techcombank", "TCB");
        bankMap.put("VPBank", "VPB");
        bankMap.put("VietinBank", "VTB");
        bankMap.put("MBBank", "MB");
        bankMap.put("Agribank", "VARB");
        bankMap.put("Sacombank", "SCB");
        bankMap.put("TPBank", "TPB");
        bankMap.put("VIB", "VIB");
        bankMap.put("BacABank", "BAB");
        bankMap.put("Eximbank", "EIB");
        bankMap.put("OceanBank", "OJB");
        bankMap.put("SHB", "SHB");
        bankMap.put("BVB", "BVB");
        bankMap.put("ABBank", "ABB");
        bankMap.put("PVcomBank", "PVCB");
    }

    public Map<String, String> getBankList() {
        return this.bankMap;
    }

    public List<String> getBankNames() {
        return List.copyOf(this.bankMap.keySet());
    }

    public List<String> getBankCodes() {
        return List.copyOf(this.bankMap.values());
    }

    public String getBankCode(String bankName) {
        return this.bankMap.get(bankName);
    }

    public String getBankName(String bankCode) {
        for (String key : this.bankMap.keySet()) {
            if (this.bankMap.get(key).equals(bankCode)) {
                return key;
            }
        }
        return null;
    }

    @Override
    protected String doInBackground(String... strings) {
        String bankCode = strings[0];
        String accountNumber = strings[1];
        return getUserByNumberAndBank(bankCode, accountNumber);
    }

    public String getUserByNumberAndBank(String bankCode, String accountNumber) {
        String curl = String.format(
                "curl --location 'https://api.banklookup.net/api/bank/id-lookup-prod' --header 'x-api-key: %s' --header 'x-api-secret: %s' --header 'Content-Type: application/json' --data '{\"bank\": \"%s\", \"account\": \"%s\"}'",
                api_key, api_secret, bankCode, accountNumber);

        try {
            URL url = new URL("https://api.banklookup.net/api/bank/id-lookup-prod");
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("POST");
            con.setRequestProperty("x-api-key", api_key);
            con.setRequestProperty("x-api-secret", api_secret);
            con.setRequestProperty("Content-Type", "application/json");
            con.setDoOutput(true);

            String jsonInputString = String.format("{\"bank\": \"%s\", \"account\": \"%s\"}", bankCode, accountNumber);
            try (java.io.OutputStream os = con.getOutputStream()) {
                byte[] input = jsonInputString.getBytes("utf-8");
                os.write(input, 0, input.length);
            }

            int responseCode = con.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream(), "utf-8"));
                StringBuilder response = new StringBuilder();
                String responseLine;
                while ((responseLine = in.readLine()) != null) {
                    response.append(responseLine.trim());
                }
                in.close();

                JSONObject jsonResponse = new JSONObject(response.toString());
                return jsonResponse.getString("ownerName");
            } else {
                throw new RuntimeException("Failed : HTTP error code : " + responseCode);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
