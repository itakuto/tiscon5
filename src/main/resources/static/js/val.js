$(function(){
  var $cap = 200 - $("#bed").val()*20 - $("#bicycle").val()*15 - $("#washingMachine").val()*10;
  $("#bed, #bicycle, #washingMachine, #box").on("keyup change",function(){
      $cap = 200 - $("#bed").val()*20 - $("#bicycle").val()*15 - $("#washingMachine").val()*10;
      $("#box_max").text("（上記の荷物の場合の上限："+$cap+"個）");
      if($cap < 0){
        $("#box_cap").text("*上記の荷物は、トラック容量を超えているため、お見積りができません");
        $("#box_max").text("");
      }
      else if($("#box").val() > $cap){
        $("#box_cap").text("*上記の荷物の場合、使えるダンボールの上限は"+$cap+"個です");
      }
      else{
        $("#box_cap").text("");
      }
  });
  $("form").validate({
    rules:{
      customerName:{
        required:true
      },
      tel:{
        required:true,
        number:true
      },
      email:{
        required:true,
        email:true
      },
      oldPost:{
        number:true
      },
      oldPrefectureId:{
        required:true
      },
      oldAddress:{
        required:true
      },
      newPost:{
        number:true
      },
      newPrefectureId:{
        required:true,
        number:true
      },
      newAddress:{
        required:true
      },
      box:{
        required:true,
        //max:$cap,
        number:true
      },
      bed:{
        required:true
      },
      bicycle:{
        required:true
      },
      washingMachine:{
        required:true
      }
    },
    messages:{
      customerName:{
        required:"<br>*氏名が入力されていません"
      },
      tel:{
        required:"<br>*電話番号が入力されていません",
        number:"<br>*ハイフン無しの半角数字で入力して下さい"
      },
      email:{
        required:"<br>*メールアドレスが入力されていません",
        email:"<br>*Eメールの形式で入力して下さい"
      },
      oldPost:{
        number:"<br>*半角数字で入力してください"
      },
      oldPrefectureId:{
        required:"<br>*転居元住所（都道府県）が入力されていません"
      },
      oldAddress:{
        required:"<br>*転居元住所（市区町村以下）が入力されていません"
      },
      newPost:{
        number:"<br>*ハイフン無しの半角数字で入力してください"
      },
      newPrefectureId:{
        required:"<br>*転居先住所（都道府県）が入力されていません"
      },
      newAddress:{
        required:"<br>*転居先住所（市区町村以下）が入力されていません"
      },
      box:{
        required:"<br>*段ボールの個数が入力されていません",
        //max:"<br>*上記の荷物の場合、使えるダンボールの上限は"+$cap+"個です",
        number:"<br>*半角数字で入力してください"
      },
      bed:{
        required:"<br>*ベッドの個数が入力されていません"
      },
      bicycle:{
        required:"<br>*自転車の個数が入力されていません"
      },
      washingMachine:{
        required:"<br>*洗濯機の個数が入力されていません"
      }
    },
    errorPlacement: function(err, elem){
        err.insertAfter(elem);
    }
  });
  $("form").submit(function(){
    if($("#box").val() > $cap){
        return false;
    }
  });
  var easy_price_min, easy_price_max, dis_min, dis_max;
  $("#check").click(function(){
    console.log("hogehoge");
    console.log($("#dis").val());
    var i = $("#dis").val();
    switch(i){
        case "0":
            console.log("hogeohoge");
            dis_min = 0;
            dis_max = 99;
            break;
        case "1":
            dis_min = 100;
            dis_max = 399;
            break;
        case "2":
            dis_min = 400;
            dis_max = 699;
            break;
        case "3":
            dis_min = 700;
            dis_max = 1500;
            break;
        default :
            console.log("hello");
    }
    console.log(dis_min);
    easy_price_min = dis_min*100 + 30000; //2トントラック3万円
    easy_price_max = dis_max*100 + 53000; //4トントラック5万円＋オプション3000円
    if($("#month").val() == 0){
        easy_price_min *= 1.8;
        easy_price_max *= 1.8; //引っ越しが多い時期の高騰
    }
    $("#result").text("お引越し費用は、"+easy_price_min+"～"+easy_price_max+"円です。");
  });
});
