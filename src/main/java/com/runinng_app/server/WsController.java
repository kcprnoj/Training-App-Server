package com.runinng_app.server;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.runinng_app.server.db.User;
import com.runinng_app.server.db.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.apache.tomcat.util.json.JSONParser;
import org.apache.tomcat.util.json.ParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;

import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;

import java.util.Arrays;
import java.util.HashMap;


@Controller
@Slf4j
public class WsController {
    @Autowired
    private UserRepository repo;

    @MessageMapping("/login")
    @SendToUser("/queue/login")
    public String processLogin(String json, SimpMessageHeaderAccessor headers) {
        System.out.println(json);
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode response = mapper.createObjectNode();
        try {
            BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
            JSONParser parser = new JSONParser(json);
            HashMap<String, Object> parsedJson = parser.object();

            User user = repo.findByLogin((String) parsedJson.get("login"));

            if (passwordEncoder.matches((String)parsedJson.get("password"), user.getPassword())) {
                response.put("Successful", "True");
                response.put("weight", user.getWeight().toString());
                response.put("height", user.getHeight().toString());
                response.put("login", user.getLogin());
                response.put("sex", user.getSex());
                ServerApplication.activeUsers.put(headers.getSessionId(), user.getLogin());
                return response.toString();
            }


        } catch (ParseException pe) {
            System.out.println(Arrays.toString(pe.getStackTrace()));
            response.put("Successful", "Parse Exception");
            return response.toString();
        } catch (Exception e) {
            System.out.println(Arrays.toString(e.getStackTrace()));
            response.put("Successful", "Exception");
            return response.toString();
        }

        response.put("Successful", "False");
        return response.toString();
    }

    @MessageMapping("/register")
    @SendToUser("/queue/register")
    public String processRegister(String json) {
        System.out.println(json);
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode response = mapper.createObjectNode();
        try {
            BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
            JSONParser parser = new JSONParser(json);
            HashMap<String, Object> parsedJson = parser.object();

            User user = new User();

            user.setLogin((String) parsedJson.get("login"));
            user.setPassword(passwordEncoder.encode((String) parsedJson.get("password")));
            user.setWeight(Float.parseFloat((String) parsedJson.get("weight")));
            user.setSex((String) parsedJson.get("sex"));
            user.setHeight(Float.parseFloat((String) parsedJson.get("height")));
            repo.save(user);

        } catch (ParseException pe) {
            System.out.println(Arrays.toString(pe.getStackTrace()));
            response.put("Successful", "Parse Exception");
            return response.toString();
        } catch (Exception e) {
            System.out.println(Arrays.toString(e.getStackTrace()));
            response.put("Successful", "Exception");
            return response.toString();
        }

        System.out.println("Successfuly added to database" + json);

        response.put("Successful", "True");
        return response.toString();
    }

    @MessageMapping("/modify")
    @SendToUser("/queue/modify")
    public String processModify(String json) {
        System.out.println(json);
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode response = mapper.createObjectNode();
        try {
            BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
            JSONParser parser = new JSONParser(json);
            HashMap<String, Object> parsedJson = parser.object();

            User old_user = repo.findByLogin((String) parsedJson.get("login"));
            User user = new User();

            if (passwordEncoder.matches((String)parsedJson.get("password"), old_user.getPassword())) {
                if (parsedJson.containsKey("new password") && parsedJson.get("new password") != null
                        && parsedJson.get("new password") != "") {
                    user.setPassword(passwordEncoder.encode((String) parsedJson.get("new password")));
                } else {
                    user.setPassword(old_user.getPassword());
                    System.out.println(user.getPassword());
                }

                if (parsedJson.containsKey("new height") && parsedJson.get("new height") != null
                        && parsedJson.get("new height") != "") {
                    user.setHeight(Float.parseFloat((String) parsedJson.get("new height")));
                } else {
                    user.setHeight(old_user.getHeight());
                    System.out.println(user.getHeight());
                }

                if (parsedJson.containsKey("new login") && parsedJson.get("new login") != null
                        && parsedJson.get("new login") != "") {
                    user.setLogin((String) parsedJson.get("new login"));
                } else {
                    user.setLogin(old_user.getLogin());
                    System.out.println(user.getLogin());
                }

                if (parsedJson.containsKey("new weight") && parsedJson.get("new weight") != null
                    && parsedJson.get("new weight") != "") {
                    user.setWeight(Float.parseFloat((String) parsedJson.get("new weight")));
                } else {
                    user.setWeight(old_user.getWeight());
                    System.out.println(user.getWeight());
                }

                if (parsedJson.containsKey("new sex") && parsedJson.get("new sex") != null
                        && parsedJson.get("new sex") != "") {
                    user.setSex((String) parsedJson.get("new sex"));
                } else {
                    user.setSex(old_user.getSex());
                    System.out.println(user.getSex());
                }

                response.put("Successful", "True");
                response.put("weight", user.getWeight().toString());
                response.put("height", user.getHeight().toString());
                response.put("login", user.getLogin());
                response.put("sex", user.getSex());

                repo.delete(old_user);
                repo.save(user);
            } else {
                response.put("Successful", "Fail");
            }

        } catch (ParseException pe) {
            System.out.println(Arrays.toString(pe.getStackTrace()));
            response.put("Successful", "Parse Exception");
        } catch (Exception e) {
            System.out.println(Arrays.toString(e.getStackTrace()));
            response.put("Successful", "Exception");
        }

        System.out.println("Successful changed in database" + json);

        return response.toString();
    }

    @MessageMapping("/delete")
    @SendToUser("/queue/delete")
    public String processDelete(String json) {
        System.out.println(json);
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode response = mapper.createObjectNode();
        try {
            BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
            JSONParser parser = new JSONParser(json);
            HashMap<String, Object> parsedJson = parser.object();

            User old_user = repo.findByLogin((String) parsedJson.get("login"));

            if (passwordEncoder.matches((String)parsedJson.get("password"), old_user.getPassword())) {
                response.put("Successful", "True");
                repo.delete(old_user);
            } else {
                response.put("Successful", "Fail");
            }

        } catch (ParseException pe) {
            System.out.println(Arrays.toString(pe.getStackTrace()));
            response.put("Successful", "Parse Exception");
        } catch (Exception e) {
            System.out.println(Arrays.toString(e.getStackTrace()));
            response.put("Successful", "Exception");
        }

        System.out.println("Successful delete " + json);

        return response.toString();
    }
}
