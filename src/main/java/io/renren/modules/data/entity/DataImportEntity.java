package io.renren.modules.data.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * 
 * 
 * @author Jack
 * @date 2023-12-20 18:59:34
 */
@Data
@TableName("data_import")
public class DataImportEntity implements Serializable {
	private static final long serialVersionUID = 1L;

	/**
	 * 
	 */
	@TableId
	private Long id;
	/**
	 * 
	 */
	private String item;
	/**
	 * 
	 */
	private String channel;
	/**
	 * 
	 */
	private String retailer;
	/**
	 * 
	 */
	private String storeCodeCustomer;
	/**
	 * 
	 */
	private String storeCodeSff;
	/**
	 * 
	 */
	private String subChannel;
	/**
	 * 
	 */
	private String storeName;
	/**
	 * 
	 */
	private String chainStoreOrNot;
	/**
	 * 
	 */
	private String customer;
	/**
	 * 
	 */
	private String province;
	/**
	 * 
	 */
	private String cityTier;
	/**
	 * 
	 */
	private String strongHold;
	/**
	 * 
	 */
	private String battleField;
	/**
	 * 
	 */
	private String greenSpace;
	/**
	 * 
	 */
	private String city1;
	/**
	 * 
	 */
	private String city2;
	/**
	 * 
	 */
	private String location;
	/**
	 * 
	 */
	private String distributeFromWhichDc;
	/**
	 * 
	 */
	private String customerRegion;
	/**
	 * 
	 */
	private String storeStatus;
	/**
	 * 
	 */
	private String active;
	/**
	 * 
	 */
	private String inactive;
	/**
	 * 
	 */
	private String attrited;
	/**
	 * 
	 */
	private String closed;
	/**
	 * 
	 */
	private String storeClassification;
	/**
	 * 
	 */
	private String f;
	/**
	 * 
	 */
	private String a;
	/**
	 * 
	 */
	private String b;
	/**
	 * 
	 */
	private String c;
	/**
	 * 
	 */
	private String d;
	/**
	 * 
	 */
	private String display;
	/**
	 * 
	 */
	private String cooler;
	/**
	 * 
	 */
	private String frozenArea;
	/**
	 * 
	 */
	private String posm;
	/**
	 * 
	 */
	private String salesPromoter;
	/**
	 * 
	 */
	private String tasting;
	/**
	 * 
	 */
	private String kam;
	/**
	 * 
	 */
	private String productBrand;
	/**
	 * 
	 */
	private String firstParty;
	/**
	 * 
	 */
	private String coBrand;
	/**
	 * 
	 */
	private String customerBrand;
	/**
	 * 
	 */
	private String productCategories;
	/**
	 * 
	 */
	private String frozenBeef;
	/**
	 * 
	 */
	private String chilledPs;
	/**
	 * 
	 */
	private String syb;
	/**
	 * 
	 */
	private String frozenLamb;
	/**
	 * 
	 */
	private String chilledLamb;
	/**
	 * 
	 */
	private String venison;
	/**
	 * 
	 */
	private String angus;
	/**
	 * 
	 */
	private String reserve;
	/**
	 * 
	 */
	private String productPackage;
	/**
	 * 
	 */
	private String entry;
	/**
	 * 
	 */
	private String uprise;
	/**
	 * 
	 */
	private String scale;
	/**
	 * 
	 */
	private String giftPack;
	/**
	 * 
	 */
	private String noOfSku;
	/**
	 * 
	 */
	private String customerSkuDescription;
	/**
	 * 
	 */
	private String skuDescription;
	/**
	 * 
	 */
	private String beefCube500g;
	/**
	 * 
	 */
	private String beefHotpotRoll200g;
	/**
	 * 
	 */
	private String beefMince500g;
	/**
	 * 
	 */
	private String beefShankMeat1000g;
	/**
	 * 
	 */
	private String beefTail500g;
	/**
	 * 
	 */
	private String lambCubeBoneIn500g;
	/**
	 * 
	 */
	private String lambFrenchRack200g;
	/**
	 * 
	 */
	private String lambLegSteak1000g;
	/**
	 * 
	 */
	private String nebInStoreWeighing;
	/**
	 * 
	 */
	private String psBeefFlatInStoreWeighing;
	/**
	 * 
	 */
	private String ribEyeSteak150g;
	/**
	 * 
	 */
	private String ribEyeSteak500g;
	/**
	 * 
	 */
	private String striploinSteak150g;
	/**
	 * 
	 */
	private String striploinSteak500g;
	/**
	 * 
	 */
	private String venisonFlapCubes1000g;
	/**
	 * 
	 */
	private String venisonFlapCubes400g;
	/**
	 * 
	 */
	private String venisonShankMeat800g;
	/**
	 * 
	 */
	private String venisonFrenchRacks1000g;
	/**
	 * 
	 */
	private String venisonFrenchRacks450g;
	/**
	 * 
	 */
	private String venisonRibs500g;
	/**
	 * 
	 */
	private String venisonShank800g;
	/**
	 * 
	 */
	private String venisonStriploin150g;
	/**
	 * 
	 */
	private String chilledLambFrRackWeighting;
	/**
	 * 
	 */
	private String venisonGiftBox;
	/**
	 * 
	 */
	private String lambCcs;
	/**
	 * 
	 */
	private String date;
	/**
	 * 
	 */
	private String year;
	/**
	 * 
	 */
	private String month;
	/**
	 * 
	 */
	private String day;
	/**
	 * 
	 */
	private String salesValue;
	/**
	 * 
	 */
	private String salesWeight;
	/**
	 * 
	 */
	private String salesQty;
	/**
	 * 
	 */
	private String packSize;
	/**
	 * 
	 */
	private String claimRate;
	/**
	 * 
	 */
	private Long createUserId;
	/**
	 * 
	 */
	private Date createTime;
	/**
	 * 
	 */
	private Integer status;

}
