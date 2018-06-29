import com.concur.unity.switchs.SwitchHelper;
import java.io.IOException;

/**
 * @description: 测试类
 * @author: Jake
 * @create: 2018-06-29 22:49
 **/
public class Config {

  public static int key = 0;

  static {
    try {
      SwitchHelper.initSwitch(Config.class);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

}
