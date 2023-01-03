import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import group.exception.AppRuntimeException;
import group.exception.ExceptionKind;
import group.dao.util.DataBaseUtil;
import group.pojo.Mission;
import group.pojo.MyTime;
import group.service.ManagerService;
import group.service.UserService;
import group.service.impl.ManagerServiceImpl;
import group.service.impl.UserServiceImpl;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.junit.Test;

import java.text.SimpleDateFormat;
import java.util.*;

public class TestMission {
    @Test
    public void testAddMission() {

        MongoCollection<Document> missionCollection =
                DataBaseUtil.getMongoDB().getCollection("Mission");

        Mission mission1 = new Mission();
        Mission mission = new Mission(
                new MyTime(2022, 12, 4, 19, 0, 20, 0),
                "东九D216", "12月例会", "吃蛋糕", new HashMap<String, Integer>() {{
            put("photo", 1);
        }}
        );

        Document document = new Document();

        document.put("missionID", mission.getMissionID());

        JSONObject time = new JSONObject();
        time.put("year", mission.getTime().getYear());
        time.put("month", mission.getTime().getMonth());
        time.put("day", mission.getTime().getDay());
        time.put("beginHour", mission.getTime().getBeginHour());
        time.put("beginMinute", mission.getTime().getBeginMinute());
        time.put("endHour", mission.getTime().getEndHour());
        time.put("endMinute", mission.getTime().getEndMinute());
        document.put("time", time);

        document.put("place", mission.getPlace());
        document.put("title", mission.getTitle());
        document.put("description", mission.getDescription());
        document.put("status", 0);

        JSONObject reporterNeeds = new JSONObject();
        for (String str : mission.getReporterNeeds().keySet()
        ) {
            reporterNeeds.put(str, mission.getReporterNeeds().get(str));
        }
        document.put("reporterNeeds", reporterNeeds);

        JSONObject reporters = new JSONObject();
        for (String str : mission.getReporters().keySet()
        ) {
            reporters.put(str, new JSONArray());
        }
        document.put("reporters", reporters);

        missionCollection.insertOne(document);

        System.out.println("插入完成");
    }

    @Test
    public void testReceiveMission() {
        JSONObject result = new JSONObject();
        String data = "{\"place\": \"sda\"," +
                "\"title\": \"fdsf\"," +
                "\"element\": \"1\"," +
                "\"description\": \"safsx\"," +
                "\"time\": {\"year\": 1988,\"month\": 12,\"day\": 12,\"beginHour\": 12,\"beginMinute\": 0,\"endHour\": 13,\"endMinute\": 0}," +
                "\"reporterNeeds\": {\"photo\": 1,\"article\": 1}}";

        System.out.println(data);

        try {
            Mission mission = JSONObject.parseObject(data, Mission.class);

            ManagerService managerService = new ManagerServiceImpl();

            managerService.addMission(mission);

            result.put("code", 202);
            result.put("msg", "任务添加成功");

        } catch (Exception e) {
            result.put("code", 203);
            result.put("msg", "任务信息错误");
            throw e;
        } finally {
            String resultStr = result.toJSONString();
            System.out.println(resultStr);
        }
    }

    @Test
    public void testQuery() {

        MongoCollection<Document> missionCollection = DataBaseUtil.getMongoDB().getCollection("Mission");


        Bson filter = Filters.eq("time.month", new Date().getMonth() + 1);

        System.out.println(new Date().getMonth() + 1);

        FindIterable<Document> findIterable = missionCollection.find(filter);

        for (Document document : findIterable) {
            document.remove("_id");
            // 计算还缺少的人数
            Document reporterNeeds = (Document) document.get("reporterNeeds");
            Document reporters = (Document) document.get("reporters");
            JSONObject reporterLack = new JSONObject();

            for (String str : reporterNeeds.keySet()
            ) {
                reporterLack.put(str, (Integer) reporterNeeds.get(str) - reporters.getList(str, String.class).size());
            }
            document.put("reporterLack", reporterLack);

            System.out.println(document);

        }
    }

    @Test
    public void testShowAll() {
        JSONObject result = new JSONObject();

        try {
            UserServiceImpl userService = new UserServiceImpl();

            JSONArray jsonArray = new JSONArray();
            jsonArray.addAll(userService.showAllMission());
            result.put("data", jsonArray);

            result.put("code", 302);
            result.put("msg", "请求显示任务成功");

        } catch (Exception e) {
            result.put("code", 303);
            result.put("msg", "请求显示任务信息错误");
        } finally {

            String resultStr = result.toJSONString();
            System.out.println(resultStr);
        }
    }

    @Test
    public void testShowNeed() {
        JSONObject result = new JSONObject();

        try {
            UserServiceImpl userService = new UserServiceImpl();

            JSONArray jsonArray = new JSONArray();
            jsonArray.addAll(userService.showNeedMission());
            result.put("data", jsonArray);

            result.put("code", 302);
            result.put("msg", "请求显示任务成功");

        } catch (Exception e) {
            result.put("code", 303);
            result.put("msg", "请求显示任务信息错误");
        } finally {

            String resultStr = result.toJSONString();
            System.out.println(resultStr);
        }
    }

    public static void doubleGet(){

        testGetMission();
        System.out.println("new request");
        testGetMission();
    }

    // get现在是多线程方法,不能用 Junit 测试
    public static void testGetMission() {

        System.out.println(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss:SSS").format(new Date()));

        String data = "{\"username\":\"test\",\"missionID\":\"12022122902\",\"kind\":\"article\"}";
        JSONObject result = new JSONObject();

        try {
            JSONObject dataJson = JSONObject.parseObject(data);

            String username = (String) dataJson.get("username");
            String missionID = (String) dataJson.get("missionID");
            String kind = (String) dataJson.get("kind");
            if (username == null || missionID == null || kind == null) {
                throw new AppRuntimeException(ExceptionKind.REQUEST_INFO_ERROR);
            }
            UserService userService = new UserServiceImpl();
            userService.getMission(username, missionID, kind);

            result.put("code", 402);
            result.put("msg", "任务参加成功");

        }catch (Exception e) {
            e.printStackTrace();
        } finally {
            String resultStr = result.toJSONString();
            System.out.println(resultStr);
        }
    }

    @Test
    public void testShowMissionById(){
        JSONObject result = new JSONObject();

        String data = "{\"missionID\":\"2345121201\"}";
        JSONObject dataJson = JSONObject.parseObject(data);
        String missionID = (String) dataJson.get("missionID");

        UserServiceImpl userService = new UserServiceImpl();

        result.put("data", userService.showMissionById(missionID));

        result.put("code", 302);
        result.put("msg", "指定查询任务成功");

        String resultStr = result.toJSONString();
        System.out.println(resultStr);
    }

    public static void main(String[] args) {

        testGetMission();
        System.out.println(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss:SSS").format(new Date()));
        //doubleGet();
    }
}
