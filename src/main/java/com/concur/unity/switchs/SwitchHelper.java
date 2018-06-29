package com.concur.unity.switchs;

import com.concur.unity.utils.ConvertUtils;
import com.concur.unity.utils.PathUtil;
import com.concur.unity.utils.ReflectionUtils;
import com.concur.unity.utils.ReflectionUtils.FieldCallback;
import com.concur.unity.utils.ReflectionUtils.FieldFilter;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Properties;

/**
 * @description: 开关控制
 * @author: Jake
 * @create: 2018-06-29 22:31
 **/
public class SwitchHelper {

  /**
   * 配置根路径(相对路径)
   */
  public static String baseDir = "/config/";

  /**
   * 初始化开关
   * @param cls 开关类
   */
  public static void initSwitch(Class<?> cls) throws IOException {
    if (cls == null) {
      throw new IllegalArgumentException("开关类不能为空:cls");
    }
    String currentDirectory = PathUtil.getCurrentWorkDirectory();
    String filePath = currentDirectory + baseDir +
        cls.getSimpleName().toLowerCase() + ".properties";
    final Properties props = new Properties();
    FileInputStream is = null;
    try {
      is = new FileInputStream(filePath);
      props.load(is);
    } finally {
      if (is != null) {
        is.close();
      }
    }

    ReflectionUtils.doWithFields(cls, new FieldCallback() {
      @Override
      public void doWith(Field field) throws IllegalArgumentException, IllegalAccessException {
        ReflectionUtils.makeAccessible(field);
        String strValue = props.getProperty(field.getName());
        field.set(null, ConvertUtils.convert(strValue, field.getType()));
      }
    }, new FieldFilter() {
      @Override
      public boolean matches(Field field) {
        return Modifier.isStatic(field.getModifiers());
      }
    });

  }

}
