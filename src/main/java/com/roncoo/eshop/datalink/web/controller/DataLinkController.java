package com.roncoo.eshop.datalink.web.controller;

import com.alibaba.fastjson.JSONObject;
import com.roncoo.eshop.datalink.serivce.EshopProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

@RestController
public class DataLinkController {
    @Autowired
    private EshopProductService eshopProductService;
    @Autowired
    private JedisPool jedisPool;
    @RequestMapping("/product")
    public String getProudct(Long productId)
    {

        //先读redis主集群
        Jedis jedis=jedisPool.getResource();
      String dimProductJSONObject= jedis.get("dim_product_"+productId);
        System.out.println(dimProductJSONObject);
        if (dimProductJSONObject==null||"".equals(dimProductJSONObject)){
            String productDataJSON =eshopProductService.findProductById(productId);
            if(productDataJSON != null && !"".equals(productDataJSON)) {
                JSONObject productDataJSONObject = JSONObject.parseObject(productDataJSON);

                String productPropertyDataJSON = eshopProductService.findProductPropertyByProductId(productId);
                if(productPropertyDataJSON != null && !"".equals(productPropertyDataJSON)) {
                    productDataJSONObject.put("product_property", JSONObject.parse(productPropertyDataJSON));
                }

                String productSpecificationDataJSON =eshopProductService.findProductSpecificationByProductId(productId);
                if(productSpecificationDataJSON != null && !"".equals(productSpecificationDataJSON)) {
                    productDataJSONObject.put("product_specification", JSONObject.parse(productSpecificationDataJSON));
                }
                jedis.set("dim_product_" + productId, productDataJSONObject.toJSONString());
                return productDataJSONObject.toJSONString();
            }


        }
        return "";
    }

}
