import com.alibaba.fastjson.JSON;
import com.facepp.error.FaceppParseException;
import com.facepp.http.HttpRequests;
import com.facepp.http.PostParameters;
import com.github.kevinsawicki.http.HttpRequest;
import me.chanjar.weixin.common.api.WxConsts;
import me.chanjar.weixin.common.exception.WxErrorException;
import me.chanjar.weixin.common.session.WxSessionManager;
import me.chanjar.weixin.mp.api.WxMpInMemoryConfigStorage;
import me.chanjar.weixin.mp.api.WxMpMessageRouter;
import me.chanjar.weixin.mp.api.WxMpService;
import me.chanjar.weixin.mp.api.WxMpServiceImpl;
import me.chanjar.weixin.mp.bean.WxMpXmlMessage;
import me.chanjar.weixin.mp.bean.WxMpXmlOutMessage;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

import static spark.Spark.*;

public class Main {

    //TODO 请替换为自己的face++ key
    static HttpRequests httpRequests = new HttpRequests("马赛克", "马赛克 ", true, true);

    public static void main(String[] args) {
        WxMpInMemoryConfigStorage configStorage = new WxMpInMemoryConfigStorage();
        //TODO 请替换为自己的微信公众号key
        configStorage.setAppId("马赛克");
        configStorage.setSecret("马赛克");
        configStorage.setToken("马赛克");
        configStorage.setAesKey("马赛克");

        WxMpService wxMpService = new WxMpServiceImpl();
        wxMpService.setWxMpConfigStorage(configStorage);

        WxMpMessageRouter router = new WxMpMessageRouter(wxMpService)

                .rule()
                .async(false)
                .msgType(WxConsts.XML_MSG_IMAGE)
                .handler(Main::imageHandler)
                .end()

                .rule()
                .async(false)
                .handler(Main::otherHandler)
                .end()

                ;



        before((req,resp) -> {
            String signature = req.queryParams("signature");
            String nonce = req.queryParams("nonce");
            String timestamp = req.queryParams("timestamp");
            if (!wxMpService.checkSignature(timestamp, nonce, signature)) {
                halt(401);
            }
        });

        post("/",(req,resp) -> router.route(WxMpXmlMessage.fromXml(req.body())).toXml());

        get("/",(req,resp) -> req.queryParams("echostr"));

    }

    static WxMpXmlOutMessage imageHandler(WxMpXmlMessage wxMessage,
                                                 Map<String, Object> context,
                                                 WxMpService wxMpService,
                                                 WxSessionManager sessionManager) throws WxErrorException {
        String result = "没识别出来";

        try {
            JSONObject json = httpRequests.recognitionIdentify(new PostParameters()
                    .setGroupName("group_test")
                    .setUrl(wxMessage.getPicUrl()));
            System.out.println(json);
            result = json.getJSONArray("face").getJSONObject(0).getJSONArray("candidate").getJSONObject(0).getString("person_name");
        } catch (FaceppParseException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }

        try {
            Map<String, String> trans = trans(result);
            result = "";
            for (String v:trans.values()) result += v+"\n";
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        return WxMpXmlOutMessage
                .TEXT()
                .content(result)
                .fromUser(wxMessage.getToUserName())
                .toUser(wxMessage.getFromUserName())
                .build();
    }

    static WxMpXmlOutMessage otherHandler(WxMpXmlMessage wxMessage,
                                       Map<String, Object> context,
                                       WxMpService wxMpService,
                                       WxSessionManager sessionManager) throws WxErrorException {
        return WxMpXmlOutMessage
                .TEXT()
                .content("请发送人脸照片（正脸）")
                .fromUser(wxMessage.getToUserName())
                .toUser(wxMessage.getFromUserName())
                .build();
    }

    static Map<String,String> trans(String person_name) throws  FileNotFoundException {

        Map<String,String> result = new HashMap();




        String person_json = new Scanner(new FileInputStream("/home/imivan/school_json/127.0.0.1:8000/" + person_name + ".json")).nextLine();

        FilePerson person = JSON.parseObject(person_json, FilePerson.class);

        if (person==null) return result;
        result
//                .put("身份证号", person.getIDNum())
                .put("班级", person.getClazz());
//        result.put("方向", person.getField());
        result.put("专业", person.getMajor());
        result.put("姓名", person.getName());
//        result.put("学号", person.getNum());
//        result.put("状态", person.getState());
        if ((person.getIDNum().charAt(person.getIDNum().length()-2)-'0')%2==0) {
            result.put("性别","妹子");
        } else {
            result.put("性别","汉子");
        }
        result.put("生日",Integer.valueOf(person.getIDNum().substring(10,12))+"月"+Integer.valueOf(person.getIDNum().substring(12,14))+"日");
        result.put("年龄","年龄"+(2015-Integer.valueOf(person.getIDNum().substring(6,10))));
        try {
            String JSONBody = HttpRequest.get("http://apis.baidu.com/apistore/idservice/id", true, "id", person.getIDNum()).header("apikey", "马赛克").body();//OTOD 请替换为自己的key
            result.put("家乡", JSON.parseObject(JSONBody, AddressJSON.class).getRetData().getAddress());
        }catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }
}
