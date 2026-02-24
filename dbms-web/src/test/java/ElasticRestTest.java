import com.alibaba.fastjson.JSON;
import org.apache.http.HttpHost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.nio.entity.NStringEntity;
import org.apache.http.util.EntityUtils;
import org.elasticsearch.client.Request;
import org.elasticsearch.client.Response;
import org.elasticsearch.client.RestClient;
import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ElasticRestTest {

    public static String parse(String requestBody) {
        String REGEX = "\\{[^\\}]*\\}";
        List<String> documents = new ArrayList<>();
        Pattern pattern = Pattern.compile(REGEX);
        Matcher matcher = pattern.matcher(requestBody);
        while (matcher.find()) {
            documents.add(matcher.group());
        }
        return JSON.toJSONString(documents);
    }
    @Test
    public void test() throws Exception {
        System.out.println("Hello World!");
        String host = "101.43.142.72";
        String port = "9200";
        String query = "{ \"index\" : { \"_id\": 2 } }\n" +
                "{ \"id\": 2, \"username\": \"test_user2\", \"password\": \"test_password2\", \"phone_number\": \"2345678901\", \"id_card\": \"234567890123456789\", \"time\": \"2022-05-12T12:00:00\", \"address\": \"456 Test St, Testville\" }\n" +
                "{ \"index\" : { \"_id\": 3 } }\n" +
                "{ \"id\": 3, \"username\": \"test_user3\", \"password\": \"test_password3\", \"phone_number\": \"3456789012\", \"id_card\": \"345678901234567890\", \"time\": \"2022-05-11T12:00:00\", \"address\": \"789 Test St, Testville\" }\n";
        System.out.println(query);
//        String REGEX = "\\{[^\\}]*\\}";
//        query = parse(query);
        RestClient client = RestClient.builder(
                new HttpHost(host, Integer.parseInt(port))).build();
        Request request = new Request("POST", "/user_test_index/_bulk");

        request.setEntity(new StringEntity(query, ContentType.APPLICATION_JSON));

        Response response = client.performRequest(request);
        String responseBody = EntityUtils.toString(response.getEntity());
        System.out.println(responseBody);
        client.close();

    }
}
