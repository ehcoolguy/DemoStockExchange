import { Component, OnInit } from '@angular/core';
import { LiveStockPriceService } from '../../live-stock-price.service';
import { NgbModal } from '@ng-bootstrap/ng-bootstrap';


@Component({
  selector: 'app-control-panel',
  templateUrl: './control-panel.component.html',
  styleUrls: ['./control-panel.component.css']
})
export class ControlPanelComponent implements OnInit {
  public btnStartCaption: string;
  public currentMsg: string;
  public currMsgs: string[] = [];

  TOPIC_PREFIX = 'D/TWSE/';
  TXT_CONNECTING = '連接中...';
  TXT_CONNECT = '啟動行情接收';
  TXT_DISCONNECTING = '停止中...';
  TXT_DISCONNECT = '停止行情接收';
  TXT_REMIND_TO_SELECT = '請點選您想要看的股票名稱來獲取即時行情。';
  TXT_ALERT_TITLE = '連接狀態';
  TXT_ALERT_CONTENT = '正在連接交易所後台，請稍後...';

  constructor(private liveStockPriceSvc: LiveStockPriceService,
    private modalService: NgbModal) { }

  ngOnInit() {
    this.btnStartCaption = this.TXT_CONNECT;
    const userLevel = this.getQueryVariable('userLevel').toLowerCase();

    console.log('Current User Level: ' + userLevel);

    if (userLevel !== 'NOT_AVAILABLE') {
      const solaceVpn = userLevel;
      const solaceClientUserName = 'customer-' + userLevel;
      const solaceClientPassword = 'password';
      this.liveStockPriceSvc.setClientCredential(solaceVpn, solaceClientUserName, solaceClientPassword);
    }
  }

  private getQueryVariable(variable) {
    const query = window.location.search.substring(1);
    const vars = query.split('&');
    for (let i = 0; i < vars.length; i++) {
      const pair = vars[i].split('=');
      if (decodeURIComponent(pair[0]) === variable) {
        return decodeURIComponent(pair[1]);
      }
    }
    return 'NOT_AVAILABLE';
  }

  public btnStartClick() {
    if (this.liveStockPriceSvc.isConnected) {
      this.btnStartCaption = this.TXT_DISCONNECTING;
      console.log('Disconnecting to SolEx backend @ ' + new Date());
      this.liveStockPriceSvc.disconnectFromSolace();
      this.btnStartCaption = this.TXT_CONNECT;
    } else {
      console.log('Connecting to SolEx backend @ ' + new Date());
      this.btnStartCaption = this.TXT_CONNECTING;
      this.liveStockPriceSvc.connectToSolace();
      alert(this.TXT_REMIND_TO_SELECT);
      this.btnStartCaption = this.TXT_DISCONNECT;
      this.liveStockPriceSvc.mySolclient.onSubscriptionOk.subscribe(ev => {
        this.currMsgs.push('SubscriptionOk RELATED: ' + ev.sEv.correlationKey);
        // this.currentMsg += '<p>SubscriptionOk RELATED: ' + ev.sEv.correlationKey + '</p>';
      });
      this.liveStockPriceSvc.mySolclient.onDisconnected.subscribe(ev => {
        this.currMsgs.push('Disconnected RELATED: ' + ev.sEv.correlationKey);
        // this.currentMsg += '<p>Disconnected RELATED: ' + ev.sEv.correlationKey + '</p>';
      });
    }
  }

  openSm(content) {
    this.modalService.open(content, { size: 'sm', centered: true });
  }

}
