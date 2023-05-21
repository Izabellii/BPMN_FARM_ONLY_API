import org.camunda.bpm.client.ExternalTaskClient;
import org.camunda.bpm.client.task.ExternalTask;
import org.camunda.bpm.client.task.ExternalTaskService;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class FarmWorker {
    public static void main(String... args) {
        String camunda_url = "https://siddhi11.bpmcep.ics.unisg.ch/engine-rest";

        // Bootstrap the client
        // Create a new ExternalTaskClient instance and provide the base URL of the Camunda engine
        ExternalTaskClient client = ExternalTaskClient.create()
                .baseUrl(camunda_url)
                .asyncResponseTimeout(1000)
                .build();

        // Subscribe to the 'get-food-label' topic and define the logic of the external task
        client.subscribe("get-food-label").handler((externalTask, externalTaskService) -> {
            try {
                // Send a GET request to the API and read the response
                // We retrieve specific food data here
                // Note: actual URL is truncated for brevity
                URL url = new URL("https://api.edamam.com/api/food-database/v2/parser?app_id=6496e02a&app_key=17c43e007b41d86a3c77460dac981958&ingr=rice&nutrition-type=cooking/hints&field=text,parsed.food.label,parsed.food.knownAs,parsed.food.nutrients,parsed.food.category,parsed.food.categoryLabel,parsed.food.image");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");

                BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                String inputLine;
                StringBuilder content = new StringBuilder();
                while ((inputLine = in.readLine()) != null) {
                    content.append(inputLine);
                }

                // Close connections
                in.close();
                conn.disconnect();

                JSONObject obj = new JSONObject(content.toString());
                JSONArray parsed = obj.getJSONArray("parsed");
                if (parsed.length() > 0) {
                    JSONObject food = parsed.getJSONObject(0).getJSONObject("food");
                    JSONObject nutrients = food.getJSONObject("nutrients");

                    double enercKcal = nutrients.getDouble("ENERC_KCAL");
                    double procnt = nutrients.getDouble("PROCNT");
                    double fat = nutrients.getDouble("FAT");
                    double chocdf = nutrients.getDouble("CHOCDF");

                    System.out.println("ENERC_KCAL: " + enercKcal);
                    System.out.println("PROCNT: " + procnt);
                    System.out.println("FAT: " + fat);
                    System.out.println("CHOCDF: " + chocdf);

                    // Create a response object
                    Map<String, Object> response = new HashMap<>();
                    response.put("enercKcal", String.valueOf(enercKcal));
                    response.put("procnt", String.valueOf(procnt));
                    response.put("fat", String.valueOf(fat));
                    response.put("chocdf", String.valueOf(chocdf));

                    // Complete the external task with the response
                    externalTaskService.complete(externalTask, response);
                } else {
                    System.out.println("No food items found in the response.");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).open();

        // Subscribe to topic get-recipe-label
        client.subscribe("get-recipe-label").handler((externalTask, externalTaskService) -> {
            try {
                URL url = new URL("https://api.edamam.com/search?q=rice&app_id=1e13fd3a&app_key=3e55b11c8bf08bb72280a68456f834df");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");

                BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                String inputLine;
                StringBuilder content = new StringBuilder();
                while ((inputLine = in.readLine()) != null) {
                    content.append(inputLine);
                }

                // Close connections
                in.close();
                conn.disconnect();

                JSONObject obj = new JSONObject(content.toString());
                JSONArray hitsArray = obj.getJSONArray("hits");
                if (hitsArray.length() > 0) {
                    JSONObject firstHit = hitsArray.getJSONObject(0);
                    JSONObject recipe = firstHit.getJSONObject("recipe");

                    // Get the "ingredientLines" array from the recipe
                    JSONArray ingredientLines = recipe.getJSONArray("ingredientLines");

                    // Save the ingredientLines in a variable called "inci_list"
                    String inci_list = ingredientLines.toString();

                    // Get the "shareAs" value from the recipe
                    String link = recipe.getString("shareAs");

                    // Create a response object
                    Map<String, Object> response = new HashMap<>();
                    response.put("inci_list", inci_list);
                    response.put("link", link);

                    // Complete the external task with the response
                    externalTaskService.complete(externalTask, response);
                } else {
                    System.out.println("No hits found.");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).open();

        // Additional topics for smart devices
        // Each subscription controls a different aspect of the device's operations

        // bootstrap the client for smart devices
        ExternalTaskClient smartDeviceClient = ExternalTaskClient.create()
                .baseUrl(camunda_url)
                .asyncResponseTimeout(1000)
                .build();
        // Subscribe to 'irrigation_on' topic and define the logic of the external task
        smartDeviceClient.subscribe("irrigation_on").handler((externalTask, externalTaskService) -> {
            String irrigation = externalTask.getVariable("irrigation");
            System.out.println("Status of system: " + irrigation);
            externalTaskService.complete(externalTask);
        }).open();

        // Subscribe to 'irrigation_off' topic and define the logic of the external task
        smartDeviceClient.subscribe("irrigation_off").handler((externalTask, externalTaskService) -> {
            String irrigation = externalTask.getVariable("irrigation");
            System.out.println("Status of system: " + irrigation);
            externalTaskService.complete(externalTask);
        }).open();

        // Subscribe to 'lamps_on' topic and define the logic of the external task
        smartDeviceClient.subscribe("lamps_on").handler((externalTask, externalTaskService) -> {
            String lamps = externalTask.getVariable("lamps");
            System.out.println("Status of system: " + lamps);
            externalTaskService.complete(externalTask);
        }).open();

        // Subscribe to 'lamps_off' topic and define the logic of the external task
        smartDeviceClient.subscribe("lamps_off").handler((externalTask, externalTaskService) -> {
            String lamps = externalTask.getVariable("lamps");
            System.out.println("Status of system: " + lamps);
            externalTaskService.complete(externalTask);
        }).open();

        // Subscribe to 'windows_on' topic and define the logic of the external task
        smartDeviceClient.subscribe("windows_on").handler((externalTask, externalTaskService) -> {
            String windows = externalTask.getVariable("windows");
            System.out.println("Status of system: " + windows);
            externalTaskService.complete(externalTask);
        }).open();

        // Subscribe to 'windows_off' topic and define the logic of the external task
        smartDeviceClient.subscribe("windows_off").handler((externalTask, externalTaskService) -> {
            String windows = externalTask.getVariable("windows");
            System.out.println("Status of system: " + windows);
            externalTaskService.complete(externalTask);
        }).open();


    }
}
