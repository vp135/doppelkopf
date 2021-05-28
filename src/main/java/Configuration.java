import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.io.*;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;

public class Configuration {




    public ConConfig connection = new ConConfig();
    public UIConfig ui = new UIConfig();
    public SkatConfig skat = new SkatConfig();
    public DokoConfig doko = new DokoConfig();
    public int logLevel=1;


    public Configuration() {

    }

    public Configuration(String name, String server, int port) {
        connection = new ConConfig(name,server,port);
    }

    public static Configuration fromFile() {
        StringBuilder builder = new StringBuilder();
        if(Files.exists(Paths.get("config.json"))) {
            try {
                FileReader reader = new FileReader("config.json");
                int c;
                while ((c = reader.read()) != -1) {
                    builder.append((char) c);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            return fromString(builder.toString());
        }
        else{
            return new Configuration();
        }
    }

    public void saveConfig() {
        try (FileWriter fw = new FileWriter("config.json");
             BufferedWriter bw = new BufferedWriter(fw);
             PrintWriter out = new PrintWriter(bw)) {
            out.println(this.toJson());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static Configuration fromString(String json){
        Gson gson = new Gson();
        Type token = new TypeToken<Configuration>(){}.getType();
        return (gson.fromJson(json,token));
    }

    public String toJson(){
        Gson gson = new GsonBuilder().serializeNulls().setPrettyPrinting().create();
        Type token = new TypeToken<Configuration>(){}.getType();
        return (gson.toJson(this,token));
    }
}
