package com.tiscon.service;

import com.tiscon.code.OptionalServiceType;
import com.tiscon.code.PackageType;
import com.tiscon.dao.EstimateDao;
import com.tiscon.domain.Customer;
import com.tiscon.domain.CustomerOptionService;
import com.tiscon.domain.CustomerPackage;
import com.tiscon.dto.UserOrderDto;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.util.Calendar;
import java.util.ArrayList;
import java.util.List;

/**
 * 引越し見積もり機能において業務処理を担当するクラス。
 *
 * @author Oikawa Yumi
 */
@Service
public class EstimateService {

    /** 引越しする距離の1 kmあたりの料金[円] */
    private static final int PRICE_PER_DISTANCE = 100;

    private final EstimateDao estimateDAO;

    /**
     * コンストラクタ
     *
     * @param estimateDAO EstimateDaoクラス
     */
    public EstimateService(EstimateDao estimateDAO) {
        this.estimateDAO = estimateDAO;
    }

    /**
     * 見積もり依頼をDBに登録する。
     *
     * @param dto 見積もり依頼情報
     */
    @Transactional
    public void registerOrder(UserOrderDto dto) {
        Customer customer = new Customer();
        BeanUtils.copyProperties(dto, customer);
        estimateDAO.insertCustomer(customer);

        if (dto.getWashingMachineInstallation()) {
            CustomerOptionService washingMachine = new CustomerOptionService();
            washingMachine.setCustomerId(customer.getCustomerId());
            washingMachine.setServiceId(OptionalServiceType.WASHING_MACHINE.getCode());
            estimateDAO.insertCustomersOptionService(washingMachine);
        }

        List<CustomerPackage> packageList = new ArrayList<>();

        packageList.add(new CustomerPackage(customer.getCustomerId(), PackageType.BOX.getCode(), dto.getBox()));
        packageList.add(new CustomerPackage(customer.getCustomerId(), PackageType.BED.getCode(), dto.getBed()));
        packageList.add(new CustomerPackage(customer.getCustomerId(), PackageType.BICYCLE.getCode(), dto.getBicycle()));
        packageList.add(new CustomerPackage(customer.getCustomerId(), PackageType.WASHING_MACHINE.getCode(), dto.getWashingMachine()));
        estimateDAO.batchInsertCustomerPackage(packageList);
    }

    /**
     * 見積もり依頼に応じた概算見積もりを行う。
     *
     * @param dto 見積もり依頼情報
     * @return 概算見積もり結果の料金
     */
    public Integer getPrice(UserOrderDto dto) {
        double distance = estimateDAO.getDistance(dto.getOldPrefectureId(), dto.getNewPrefectureId());

        //同一県であった場合(distanse == 0の場合)
        String oldAdd; //昔の住所
        String newAdd; //新しい住所
        oldAdd = dto.getOldAddress();
        newAdd = dto.getNewAddress();
        int i;
        int judge;//判定変数

        //市区町村の取り出し
        String town;
        String[] townType = {"市", "区", "町", "村"};
        for(i = 0; i < 4; i++) {
            judge = oldAdd.indexOf(townType[i]);
            if (judge != -1) {
                oldAdd = oldAdd.substring(0,judge);
                break;
            }
        }
        for(i = 0; i < 4; i++) {
            judge = newAdd.indexOf(townType[i]);
            if (judge != -1) {
                newAdd = newAdd.substring(0,judge);
                break;
            }
        }
        if(distance == 0){//同じ県のとき
            if(oldAdd.equals(newAdd)) {//同じ市区町村のときは距離考慮無しくらいがちょうどいい

            }else{//違う市区町村であるときに限り、料金を設定。(面積の大きさでもう少し細分化するかも)
                distance = 20;
            }
        }


        // 小数点以下を切り捨てる
        int distanceInt = (int) Math.floor(distance);

        // 距離当たりの料金を算出する
        int priceForDistance = distanceInt * PRICE_PER_DISTANCE;

        //要らないかもだけど、繫盛期かの判定
//        Calendar calendar = Calendar.getInstance();
//
//        int month;
//        month = calendar.get(Calendar.MONTH);
//        double busy;
//
//        if(month == 2 || month == 3 || month == 4){
//            busy = 1.8;
//        }else{
//            busy = 1.0;
//        }

        int boxes = getBoxForPackage(dto.getBox(), PackageType.BOX)
                + getBoxForPackage(dto.getBed(), PackageType.BED)
                + getBoxForPackage(dto.getBicycle(), PackageType.BICYCLE)
                + getBoxForPackage(dto.getWashingMachine(), PackageType.WASHING_MACHINE);

        // 箱に応じてトラックの種類が変わり、それに応じて料金が変わるためトラック料金を算出する。
        int pricePerTruck = estimateDAO.getPricePerTruck(boxes);

        // オプションサービスの料金を算出する。
        int priceForOptionalService = 0;

        if (dto.getWashingMachineInstallation()) {
            priceForOptionalService = estimateDAO.getPricePerOptionalService(OptionalServiceType.WASHING_MACHINE.getCode());
        }
        //承認必要かな
//        int totalPrice =(int)(busy * (priceForDistance + pricePerTruck + priceForOptionalService));
        int totalPrice = priceForDistance + pricePerTruck + priceForOptionalService;
        return totalPrice;
    }

    /**
     * 荷物当たりの段ボール数を算出する。
     *
     * @param packageNum 荷物数
     * @param type       荷物の種類
     * @return 段ボール数
     */
    private int getBoxForPackage(int packageNum, PackageType type) {
        return packageNum * estimateDAO.getBoxPerPackage(type.getCode());
    }
}