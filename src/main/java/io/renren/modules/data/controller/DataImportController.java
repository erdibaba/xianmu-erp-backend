package io.renren.modules.data.controller;

import java.text.SimpleDateFormat;
import java.util.*;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import io.renren.common.annotation.SysLog;
import io.renren.common.utils.ExcelUtils;
import io.renren.modules.sys.controller.AbstractController;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import io.renren.modules.data.entity.DataImportEntity;
import io.renren.modules.data.service.DataImportService;
import io.renren.common.utils.PageUtils;
import io.renren.common.utils.R;
import org.springframework.web.multipart.MultipartFile;


/**
 * 
 *
 * @author chenshun
 * @email sunlightcs@gmail.com
 * @date 2023-12-20 18:59:34
 */
@RestController
@RequestMapping("generator/dataimport")
public class DataImportController  extends AbstractController {
    @Autowired
    private DataImportService dataImportService;

    /**
     * 列表
     */
    @RequestMapping("/list")
    @RequiresPermissions("generator:dataimport:list")
    public R list(@RequestParam Map<String, Object> params){
        params.put("createUserId",getUserId());
        PageUtils page = dataImportService.queryPage(params);

        return R.ok().put("page", page);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{id}")
    @RequiresPermissions("generator:dataimport:info")
    public R info(@PathVariable("id") Long id){
		DataImportEntity dataImport = dataImportService.getById(id);

        return R.ok().put("dataImport", dataImport);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    @RequiresPermissions("generator:dataimport:save")
    public R save(@RequestBody DataImportEntity dataImport){
		dataImportService.save(dataImport);

        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    @RequiresPermissions("generator:dataimport:update")
    public R update(@RequestBody DataImportEntity dataImport){
		dataImportService.updateById(dataImport);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    @RequiresPermissions("generator:dataimport:delete")
    public R delete(@RequestBody Long[] ids){
		dataImportService.removeByIds(Arrays.asList(ids));

        return R.ok();
    }

    @SysLog("导入Data")
    @PostMapping(value = "/dynamicImportExcel")
    @ResponseBody
    public R dynamicImportExcel(MultipartFile file){
        //获取导入数据
        List<Map<String,Object>> list = ExcelUtils.dynamicImportExcel(file,0);
        StringBuilder msg = new StringBuilder();
        Date date = new Date();
        if(list.size()>0){
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

            dataImportService.delDataImport(getUserId(),sdf.format(date));
            // 开始导入
            List<DataImportEntity> dataImportList = new ArrayList<>();
            for (Map<String,Object> maps : list){
                //循环表头，set值
                DataImportEntity entity = new DataImportEntity();
                addEntity(maps,entity,date,getUserId());
                dataImportList.add(entity);
            }
            dataImportService.insertBatch(dataImportList);
        }else{
            return R.error("未检测到数据");
        }
        return R.ok();
    }

    private void addEntity(Map<String,Object> maps,DataImportEntity entity,Date date,Long userId){
        entity.setItem(maps.get("Item")!=null?maps.get("Item").toString():"" );
        entity.setChannel(maps.get("Channel")!=null?maps.get("Channel").toString():"" );
        entity.setRetailer(maps.get("Retailer")!=null?maps.get("Retailer").toString():"" );
        entity.setStoreCodeCustomer(maps.get("Store Code-Customer")!=null?maps.get("Store Code-Customer").toString():"" );
        entity.setStoreCodeSff(maps.get("Store Code-SFF")!=null?maps.get("Store Code-SFF").toString():"" );
        entity.setSubChannel(maps.get("Sub Channel")!=null?maps.get("Sub Channel").toString():"" );
        entity.setStoreName(maps.get("Store Name")!=null?maps.get("Store Name").toString():"" );
        entity.setChainStoreOrNot(maps.get("Chain Store or not")!=null?maps.get("Chain Store or not").toString():"" );
        entity.setCustomer(maps.get("Customer")!=null?maps.get("Customer").toString():"" );
        entity.setProvince(maps.get("Province")!=null?maps.get("Province").toString():"" );
        entity.setCityTier(maps.get("City Tier")!=null?maps.get("City Tier").toString():"" );
        entity.setStrongHold(maps.get("Strong hold")!=null?maps.get("Strong hold").toString():"" );
        entity.setBattleField(maps.get("Battle Field")!=null?maps.get("Battle Field").toString():"" );
        entity.setGreenSpace(maps.get("Green space")!=null?maps.get("Green space").toString():"" );
        entity.setCity1(maps.get("City1(地级市名)")!=null?maps.get("City1(地级市名)").toString():"" );
        entity.setCity2(maps.get("City2(城市名/直辖市区）")!=null?maps.get("City2(城市名/直辖市区）").toString():"" );
        entity.setLocation(maps.get("Location")!=null?maps.get("Location").toString():"" );
        entity.setDistributeFromWhichDc(maps.get("Distribute from which DC")!=null?maps.get("Distribute from which DC").toString():"" );
        entity.setCustomerRegion(maps.get("Customer Region")!=null?maps.get("Customer Region").toString():"" );
        entity.setStoreStatus(maps.get("Store Status")!=null?maps.get("Store Status").toString():"" );
        entity.setActive(maps.get("Active")!=null?maps.get("Active").toString():"" );
        entity.setInactive(maps.get("Inactive")!=null?maps.get("Inactive").toString():"" );
        entity.setAttrited(maps.get("Attrited")!=null?maps.get("Attrited").toString():"" );
        entity.setClosed(maps.get("Closed")!=null?maps.get("Closed").toString():"" );
        entity.setStoreClassification(maps.get("Store classification")!=null?maps.get("Store classification").toString():"" );
        entity.setF(maps.get("F(>=50K/Month)")!=null?maps.get("F(>=50K/Month)").toString():"" );
        entity.setA(maps.get("A(>=20K/Month)")!=null?maps.get("A(>=20K/Month)").toString():"" );
        entity.setB(maps.get("B(>=10K/Month)")!=null?maps.get("B(>=10K/Month)").toString():"" );
        entity.setC(maps.get("C(>3K/Month)")!=null?maps.get("C(>3K/Month)").toString():"" );
        entity.setD(maps.get("D(<=3K/Month)")!=null?maps.get("D(<=3K/Month)").toString():"" );
        entity.setDisplay(maps.get("Display")!=null?maps.get("Display").toString():"" );
        entity.setCooler(maps.get("Cooler")!=null?maps.get("Cooler").toString():"" );
        entity.setFrozenArea(maps.get("Frozen Area")!=null?maps.get("Frozen Area").toString():"" );
        entity.setPosm(maps.get("POSM")!=null?maps.get("POSM").toString():"" );
        entity.setSalesPromoter(maps.get("Sales Promoter")!=null?maps.get("Sales Promoter").toString():"" );
        entity.setTasting(maps.get("Tasting")!=null?maps.get("Tasting").toString():"" );
        entity.setKam(maps.get("KAM")!=null?maps.get("KAM").toString():"" );
        entity.setProductBrand(maps.get("Product Brand")!=null?maps.get("Product Brand").toString():"" );
        entity.setFirstParty(maps.get("First Party(SFF）")!=null?maps.get("First Party(SFF）").toString():"" );
        entity.setCoBrand(maps.get("Co-Brand")!=null?maps.get("Co-Brand").toString():"" );
        entity.setCustomerBrand(maps.get("Customer brand")!=null?maps.get("Customer brand").toString():"" );
        entity.setProductCategories(maps.get("Product Categories")!=null?maps.get("Product Categories").toString():"" );
        entity.setFrozenBeef(maps.get("Frozen Beef")!=null?maps.get("Frozen Beef").toString():"" );
        entity.setChilledPs(maps.get("Chilled PS")!=null?maps.get("Chilled PS").toString():"" );
        entity.setSyb(maps.get("SYB")!=null?maps.get("SYB").toString():"" );
        entity.setFrozenLamb(maps.get("Frozen Lamb")!=null?maps.get("Frozen Lamb").toString():"" );
        entity.setChilledLamb(maps.get("Chilled Lamb")!=null?maps.get("Chilled Lamb").toString():"" );
        entity.setVenison(maps.get("Venison")!=null?maps.get("Venison").toString():"" );
        entity.setAngus(maps.get("Angus")!=null?maps.get("Angus").toString():"" );
        entity.setReserve(maps.get("Reserve")!=null?maps.get("Reserve").toString():"" );
        entity.setProductPackage(maps.get("Product Package")!=null?maps.get("Product Package").toString():"" );
        entity.setEntry(maps.get("Entry(<1KG)")!=null?maps.get("Entry(<1KG)").toString():"" );
        entity.setUprise(maps.get("Uprise(1-2KG)")!=null?maps.get("Uprise(1-2KG)").toString():"" );
        entity.setScale(maps.get("Scale(>2KG)")!=null?maps.get("Scale(>2KG)").toString():"" );
        entity.setGiftPack(maps.get("Gift Pack")!=null?maps.get("Gift Pack").toString():"" );
        entity.setNoOfSku(maps.get("No. of SKU")!=null?maps.get("No. of SKU").toString():"" );
        entity.setCustomerSkuDescription(maps.get("Customer SKU description")!=null?maps.get("Customer SKU description").toString():"" );
        entity.setSkuDescription(maps.get("SKU Description")!=null?maps.get("SKU Description").toString():"" );
        entity.setBeefCube500g(maps.get("Beef Cube 500g")!=null?maps.get("Beef Cube 500g").toString():"" );
        entity.setBeefHotpotRoll200g(maps.get("Beef Hotpot Roll 200g")!=null?maps.get("Beef Hotpot Roll 200g").toString():"" );
        entity.setBeefMince500g(maps.get("Beef Mince 500g")!=null?maps.get("Beef Mince 500g").toString():"" );
        entity.setBeefShankMeat1000g(maps.get("Beef Shank Meat 1000g")!=null?maps.get("Beef Shank Meat 1000g").toString():"" );
        entity.setBeefTail500g(maps.get("Beef Tail 500g")!=null?maps.get("Beef Tail 500g").toString():"" );
        entity.setLambCubeBoneIn500g(maps.get("Lamb Cube Bone-in 500g")!=null?maps.get("Lamb Cube Bone-in 500g").toString():"" );
        entity.setLambFrenchRack200g(maps.get("Lamb French Rack 200g")!=null?maps.get("Lamb French Rack 200g").toString():"" );
        entity.setLambLegSteak1000g(maps.get("Lamb Leg steak 1000g")!=null?maps.get("Lamb Leg steak 1000g").toString():"" );
        entity.setNebInStoreWeighing(maps.get("NEB In-store Weighing")!=null?maps.get("NEB In-store Weighing").toString():"" );
        entity.setPsBeefFlatInStoreWeighing(maps.get("PS Beef Flat In-store Weighing")!=null?maps.get("PS Beef Flat In-store Weighing").toString():"" );
        entity.setRibEyeSteak150g(maps.get("Rib Eye Steak 150g")!=null?maps.get("Rib Eye Steak 150g").toString():"" );
        entity.setRibEyeSteak500g(maps.get("Rib Eye Steak 500g")!=null?maps.get("Rib Eye Steak 500g").toString():"" );
        entity.setStriploinSteak150g(maps.get("Striploin Steak 150g")!=null?maps.get("Striploin Steak 150g").toString():"" );
        entity.setStriploinSteak500g(maps.get("Striploin Steak 500g")!=null?maps.get("Striploin Steak 500g").toString():"" );
        entity.setVenisonFlapCubes1000g(maps.get("Venison Flap Cubes 1000g")!=null?maps.get("Venison Flap Cubes 1000g").toString():"" );
        entity.setVenisonFlapCubes400g(maps.get("Venison Flap Cubes 400g")!=null?maps.get("Venison Flap Cubes 400g").toString():"" );
        entity.setVenisonShankMeat800g(maps.get("Venison Shank Meat 800g")!=null?maps.get("Venison Shank Meat 800g").toString():"" );
        entity.setVenisonFrenchRacks1000g(maps.get("Venison French Racks 1000g")!=null?maps.get("Venison French Racks 1000g").toString():"" );
        entity.setVenisonFrenchRacks450g(maps.get("Venison French Racks 450g")!=null?maps.get("Venison French Racks 450g").toString():"" );
        entity.setVenisonRibs500g(maps.get("Venison Ribs 500g")!=null?maps.get("Venison Ribs 500g").toString():"" );
        entity.setVenisonShank800g(maps.get("Venison Shank 800g")!=null?maps.get("Venison Shank 800g").toString():"" );
        entity.setVenisonStriploin150g(maps.get("Venison Striploin 150g")!=null?maps.get("Venison Striploin 150g").toString():"" );
        entity.setChilledLambFrRackWeighting(maps.get("Chilled Lamb Fr. Rack-Weighting")!=null?maps.get("Chilled Lamb Fr. Rack-Weighting").toString():"" );
        entity.setVenisonGiftBox(maps.get("Venison Gift Box")!=null?maps.get("Venison Gift Box").toString():"" );
        entity.setLambCcs(maps.get("Lamb CCS")!=null?maps.get("Lamb CCS").toString():"" );
        entity.setDate(maps.get("Date")!=null?maps.get("Date").toString():"" );
        entity.setYear(maps.get("Year")!=null?maps.get("Year").toString():"" );
        entity.setMonth(maps.get("Month")!=null?maps.get("Month").toString():"" );
        entity.setDay(maps.get("Day")!=null?maps.get("Day").toString():"" );
        entity.setSalesValue(maps.get("Sales Value")!=null?maps.get("Sales Value").toString():"" );
        entity.setSalesWeight(maps.get("Sales Weight")!=null?maps.get("Sales Weight").toString():"" );
        entity.setSalesQty(maps.get("Sales Qty(PC）")!=null?maps.get("Sales Qty(PC）").toString():"" );
        entity.setPackSize(maps.get("Pack Size(g)")!=null?maps.get("Pack Size(g)").toString():"" );
        entity.setClaimRate(maps.get("Claim Rate")!=null?maps.get("Claim Rate").toString():"" );
        entity.setCreateUserId(userId);
        entity.setCreateTime(date);
        entity.setStatus(1);
    }

}
