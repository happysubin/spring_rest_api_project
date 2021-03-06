package springapi.api.controller;

import com.fasterxml.jackson.databind.ser.FilterProvider;
import com.fasterxml.jackson.databind.ser.impl.SimpleBeanPropertyFilter;
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.converter.json.MappingJacksonValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import springapi.api.dao.UserDao;
import springapi.api.domain.User;
import springapi.api.domain.UserV2;
import springapi.api.exception.UserNotFoundException;

import java.util.List;

@RestController
@RequestMapping("/admin")
public class AdminController {
    private UserDao userDao;

    @Autowired
    public AdminController(UserDao userDao) {
        this.userDao = userDao;
    }

    @GetMapping("/users")
    public MappingJacksonValue retrieveAllUsers(){

        List<User> users = userDao.findAll();

        SimpleBeanPropertyFilter filter = SimpleBeanPropertyFilter
                .filterOutAllExcept("id","name","joinDate","ssn");

        FilterProvider filters = new SimpleFilterProvider().addFilter("UserInfo",filter);

        MappingJacksonValue mapping = new MappingJacksonValue(users);
        mapping.setFilters(filters);

        return mapping;
    }
    //@GetMapping("/v1/users/{id}") uri
    //@GetMapping(value="/users/{id}/",params="version=1") 파라미터 http://localhost:8088/admin/users/1/?version=1
    //@GetMapping(value="/users/{id}",headers = "X-API-VERSION=1") //헤더 필드 X-API-VERSION 추가 value에 1을 넣는다.
    @GetMapping(value="/users/{id}",produces = "application/vnd.company.appv1+json") //헤더에 Accept
    public MappingJacksonValue retrieveUserV1(@PathVariable Long id){                   //value는 application/vnd.company.appv1+json
        User user = userDao.findOne(id);

        if(user == null){
            throw new UserNotFoundException(String.format("ID[%s] not found",id));
        }

        SimpleBeanPropertyFilter filter = SimpleBeanPropertyFilter
                .filterOutAllExcept("id","name","joinDate","ssn");

        FilterProvider filters = new SimpleFilterProvider().addFilter("UserInfo",filter);

        MappingJacksonValue mapping = new MappingJacksonValue(user);
        mapping.setFilters(filters);


        return mapping;
    }

    //@GetMapping("/v2/users/{id}")
    //@GetMapping(value="/users/{id}/",params="version=2") http://localhost:8088/admin/users/1/?version=2
    //@GetMapping(value="/users/{id}",headers = "X-API-VERSION=2")
    @GetMapping(value="/users/{id}",produces = "application/vnd.company.appv2+json") //mimetype를 이용
    public MappingJacksonValue retrieveUserV2(@PathVariable Long id){
        User user = userDao.findOne(id);

        if(user == null){
            throw new UserNotFoundException(String.format("ID[%s] not found",id));
        }

        UserV2 userV2 = new UserV2();

        BeanUtils.copyProperties(user,userV2); //스프링에서 제공하는 빈 작업을 도와주는 클래스
        userV2.setGrade("VIP");

        SimpleBeanPropertyFilter filter = SimpleBeanPropertyFilter
                .filterOutAllExcept("id","name","ssn","joinDate","grade");

        FilterProvider filters = new SimpleFilterProvider().addFilter("UserInfoV2",filter);

        MappingJacksonValue mapping = new MappingJacksonValue(userV2);
        mapping.setFilters(filters);


        return mapping;
    }
}
