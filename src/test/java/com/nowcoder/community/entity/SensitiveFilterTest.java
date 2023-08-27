package com.nowcoder.community.entity;


import com.nowcoder.community.CommunityApplication;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;


@RunWith(SpringRunner.class)
@SpringBootTest
@ContextConfiguration(classes = CommunityApplication.class)
public class SensitiveFilterTest {
    @Autowired
    private SensitiveFilter sensitiveFilter;

    @Test
    public void testSensitiveFilter(){
        String text = "这里可以赌博，可以吸毒，可以嫖娼,继续jldfjaljg赌***&博";
        text = sensitiveFilter.filter(text);
        System.out.println(text);
    }

}