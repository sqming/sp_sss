 package com.jt.service;

import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestMapping;

import com.alibaba.dubbo.config.annotation.Service;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.jt.mapper.CartMapper;
import com.jt.pojo.Cart;

//编辑提供者jt-cart中的实现类
@Service(timeout=3000)
public class DubboCartServiceImpl implements DubboCartService{
	@Autowired
	private CartMapper cartMapper;

	@Override
	public List<Cart> findCartListByUserId(Long userId) {
		// TODO Auto-generated method stub
		QueryWrapper<Cart> queryWrapper=new QueryWrapper<Cart>();
		queryWrapper.eq("user_id",userId);
		return cartMapper.selectList(queryWrapper);
	}
	/**
	 * 更新数据信息：num/updated
	 * 判断条件：where user_id and item_id
	 */
	@Transactional
	@Override
	public void updateCartNum(Cart cart) {
		// TODO Auto-generated method stub
		 Cart tempCart=new Cart();
		 tempCart.setNum(cart.getNum())
		 		.setUpdated(new Date());
		 UpdateWrapper<Cart> updateWrapper=new UpdateWrapper<Cart>();
		 updateWrapper.eq("user_id",cart.getUserId())
		 .eq("item_id",cart.getItemId());
		 cartMapper.update(tempCart, updateWrapper);
	}
	/**
	 * 条件构造器中将对象中不为空的属性当做where条件
	 * 前提：保证cart中只能有2个属性不为null
	 */
	@Override
	public void deleteCart(Cart cart) {
		// TODO Auto-generated method stub
		QueryWrapper<Cart> queryWrapper=new QueryWrapper<Cart>(cart);
		cartMapper.delete(queryWrapper);
	}
	/**
	 * 新增业务实现
	 * 1.用户第一次新新增可以直接入库
	 * 2.用户不是第一次入库.只做数量的修改
	 * 
	 * 根据itemId和userId查询数据
	 */
	@Override
	public void insertCart(Cart cart) {
		// TODO Auto-generated method stub
		QueryWrapper<Cart> queryWrapper=new QueryWrapper<Cart>();
		queryWrapper.eq("user_id",cart.getUserId())
					.eq("item_id",cart.getItemId());
		Cart cartDB=cartMapper.selectOne(queryWrapper);
		if(cartDB==null) {
			//用户第一次购买商品 可以直接入库
			cart.setCreated(new Date())
				.setUpdated(cart.getCreated());
			cartMapper.insert(cart);
		}else {
			//表示用户多出添加购物车 只做数量的修改
			//需要添加的数量加上数据库原有的数量
			int num=cart.getNum()+cartDB.getNum();
			cartDB.setNum(num).setUpdated(new Date());
			cartMapper.updateById(cartDB);
			/**
			 * 衍生知识 
			 * 修改操作时，除了主键之外都要更新不为null的数据
			 * sql：update tb_cart set num=#{num},updated=#{updated},
			 * 						user_Id=#{user_Id}XXXXXXXXXXX
			 * 						where id=#{id}
			 * 目的：
			 * 		有时自己手写sql可能效果更好				
			 */
		}
	}
	
}
