import java.net.MalformedURLException;
import java.net.URL;

public class UrlTest {
    public static void main(String[] args) throws MalformedURLException {
        URL u = new URL("jdbc:mysql://192.168.20.229:3306/hjgp-szdk?autoReconnect=true&useUnicode=true&characterEncoding=UTF-8&zeroDateTimeBehavior=convertToNull&useSSL=false&serverTimezone=Asia/Shanghai");
        System.out.println(u.getPath());
    }
}
