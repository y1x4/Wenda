package com.yixuwang;

import com.yixuwang.model.EntityType;
import com.yixuwang.service.LikeService;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 *
 * Created by yixu on 2018/7/6.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = WendaApplication.class)
public class LikeServiceTests {

    @Autowired
    LikeService likeService;

    @Before
    public void setUp() {
        System.out.println("before test");
    }

    @Test
    public void testLike() {
        likeService.like(1, EntityType.ENTITY_QUESTION, 1);
        Assert.assertEquals(1, likeService.getLikeStatus(1, EntityType.ENTITY_QUESTION, 1));

        likeService.dislike(1, EntityType.ENTITY_QUESTION, 1);
        Assert.assertEquals(-1, likeService.getLikeStatus(1, EntityType.ENTITY_QUESTION, 1));
        System.out.println("testing...");
    }


    @After
    public void after() {
        System.out.println("after test");
    }
}
