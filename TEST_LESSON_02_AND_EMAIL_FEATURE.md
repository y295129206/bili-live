# 功能说明 + 第二课讲义：邮箱验证码注册 & Mockito

> 功能：注册页邮箱验证码（QQ 邮箱）
> 教学：第二课 Mockito 打桩与 Service 层测试
> 日期：2026-11-23

---

## 第一部分：功能实现清单

### 一、完整流程

```
用户点「获取验证码」
  → 前端本地校验（非空 + QQ 邮箱格式）
  → POST /account/sendEmailCode
      → 服务端校验：格式 → 是否已在库（邮箱已注册?）→ 60秒限流
      → 生成 6 位数字码 → 存 Redis（5分钟）
      → JavaMailSender 经 QQ SMTP 发信
  → 前端按钮进入 60 秒倒计时

用户填验证码，点注册
  → POST /account/register（新增 emailCode 参数）
      → 算术验证码校验（原有）
      → 邮箱验证码校验：一致→立即删除；不一致→失败次数+1，满5次作废
      → 执行原注册写库逻辑
```

### 二、改动文件清单

**后端**
| 文件 | 改动 |
|---|---|
| `bili-common/pom.xml` | 加 `spring-boot-starter-mail` |
| `AppConfig.java` | 加 `mailFrom`（读 spring.mail.username） |
| `Constants.java` | 加 `REGEX_QQ_EMAIL`、`EMAIL_CODE_EXPIRE`、`EMAIL_CODE_SEND_INTERVAL`、`EMAIL_CODE_MAX_FAIL_COUNT`、两个 Redis key 前缀 |
| `pojo/EmailCodeDto.java`（新增） | 验证码 + 失败次数 |
| `RedisComponent.java` | 加 `saveEmailCode/getEmailCode/updateEmailCode/cleanEmailCode/setEmailCodeSendLimit` |
| `service/EmailService.java`（新增） | 接口：sendEmailCode、verifyEmailCode |
| `service/impl/EmailServiceImpl.java`（新增） | 实现：校验、生成、发信、校验、失败计数 |
| `web/AccountController.java` | 加 `/sendEmailCode`；`register` 加 `emailCode` 参数与校验；邮箱规范化 |
| `bili-web/application.yml`、`bili-admin/application.yml` | 加 `spring.mail.*` 配置 |

**前端**
| 文件 | 改动 |
|---|---|
| `utils/Api.js` | 加 `sendEmailCode` |
| `views/account/Account.vue` | 注册 tab 加邮箱验证码输入框 + 获取按钮 + 60s 倒计时；表单规则、resetForm 重置、样式 |

### 三、存储设计

| Redis Key | 值 | TTL | 用途 |
|---|---|---|---|
| `bililive:emailCode:{email}` | `EmailCodeDto{code, failCount}` | 5 分钟 | 验证码本体 + 失败计数 |
| `bililive:emailCodeLimit:{email}` | 发送时间戳 | 60 秒 | 发送间隔限流 |

### 四、⚠️ 上线前必做

配置里 `password: ${MAIL_PASSWORD:Ymz123456}` 的默认值 **Ymz123456 是 QQ 登录密码，不能用于 SMTP**。QQ 邮箱 SMTP 只认 16 位授权码。两种改法：

1. **设置环境变量** `MAIL_PASSWORD=你的16位授权码`（推荐，不改代码）
2. 直接把两个 yml 里的默认值改成授权码

获取授权码：QQ 邮箱 → 设置 → 账户 → POP3/SMTP 服务 → 开启 → 生成授权码。

> 发信失败时代码会清理验证码与限流窗口，允许用户立即重试——所以即使用错密码也不会"卡死"，只是收不到邮件。

---

## 第二部分：第二课讲义 —— Mockito 打桩与 Service 层测试

配套代码：`service/impl/EmailServiceImplTest.java`（9 个用例，全部通过）

### 2.1 为什么 Service 层需要 Mock？

第一课的 `SimilarityCalculator` 是纯函数，直接 new 即可测。但 `EmailServiceImpl` 依赖 4 个外部对象：

- `JavaMailSender` —— 真实发信要连 QQ SMTP（慢、要网络、要凭据）
- `RedisComponent` —— 依赖 Redis 服务
- `UserInfoMapper` —— 依赖 MySQL
- `AppConfig` —— 配置

单元测试要求 **快、独立、可重复**。若真连这些外部系统，测试会变慢、不稳定（网络抖动就失败）、且需要环境。解决办法：用 **Mock（模拟对象）** 替换它们，只测 `EmailServiceImpl` 自己的逻辑。

### 2.2 三个核心注解

| 注解 | 作用 |
|---|---|
| `@ExtendWith(MockitoExtension.class)` | 启用 Mockito 的注解支持（JUnit5 扩展） |
| `@Mock` | 创建一个虚拟依赖对象（如 `@Mock JavaMailSender`） |
| `@InjectMocks` | 创建被测对象，并自动把 `@Mock` 对象注入其字段 |

### 2.3 打桩（Stub）：when(...).thenReturn(...)

```java
when(userInfoMapper.findByEmail(QQ_EMAIL)).thenReturn(null); // 模拟"邮箱未注册"
when(redisComponent.setEmailCodeSendLimit(QQ_EMAIL)).thenReturn(true); // 模拟"未限流"
```

含义：当调用这个 mock 方法时，返回指定值，而不是执行真实逻辑。

**验证交互（verify）**：
```java
verify(redisComponent).saveEmailCode(eq(QQ_EMAIL), codeCaptor.capture()); // 确认确实调用了
verify(javaMailSender, never()).send(any()); // 确认某方法"从未"被调用
```

### 2.4 本次踩坑：UnnecessaryStubbingException（重要教学点）

第一次运行 9 个用例，7 个报错：

```
org.mockito.exceptions.misusing.UnnecessaryStubbingException:
Please remove unnecessary stubbings or use 'lenient' strictness.
```

**原因**：我在 `@BeforeEach` 里对所有用例统一打桩 `createMimeMessage()` 和 `getMailFrom()`。但那些"校验类"用例（如验证码过期、邮箱已注册）根本不走发信逻辑，用不到这两个桩。

Mockito 默认使用 **STRICT_STUBS 严格模式**：任何打桩如果本次用例**没有实际用到**，就判定为"多余打桩"并报错。

**三种解法**：
1. **按需打桩（推荐，本次采用）**：把发信相关的桩抽成 `stubMailSender()`，只在发信用例里调用
2. `lenient().when(...)`：声明这个桩是宽松的，允许不被使用
3. 用 `@MockitoSettings(strictness = Strictness.LENIENT)` 全局放宽（不推荐，会掩盖真实问题）

**工程启示**：严格模式是好事——它逼你保持测试精确，每条用例只声明自己需要的桩，避免测试之间隐式耦合。

### 2.5 参数捕获 ArgumentCaptor

```java
ArgumentCaptor<String> codeCaptor = ArgumentCaptor.forClass(String.class);
verify(redisComponent).saveEmailCode(eq(QQ_EMAIL), codeCaptor.capture());
String code = codeCaptor.getValue();
assertTrue(code.matches("\\d{6}"));
```

用于捕获"传给 mock 方法的实际参数"，从而断言"生成的验证码确实是 6 位数字"——这是验证**方法内部行为**而非仅验证返回值的常用技巧。

### 2.6 本次测试的用例设计

| 用例 | 场景 | 验证点 |
|---|---|---|
| sendEmailCodeSuccess | 合法邮箱、未注册、未限流 | 存码 + 发信 + 验证码是6位数字 |
| sendEmailCodeRegisteredEmail | 邮箱已注册 | 不发信、不存码、抛"邮箱已注册" |
| sendEmailCodeRejectNonQQEmail | 非QQ邮箱 | 直接拒绝，不查库不发信 |
| sendEmailCodeRateLimited | 60秒内重复 | 拒绝，不发信 |
| sendEmailCodeMailFailureCleansUp | 发信异常 | 清理验证码 + 限流窗口，允许重试 |
| verifyEmailCodeSuccessDeletesCode | 验证码正确 | 通过后立即删除（一次性） |
| verifyEmailCodeExpired | 验证码不存在 | 抛过期异常 |
| verifyEmailCodeWrongIncrementsFailCount | 输错一次 | 失败次数0→1，提示剩余4次 |
| verifyEmailCodeMaxFailDeletesCode | 第5次输错 | 验证码作废删除 |

### 2.7 面试可能的问题

**Q1：Mock 和 Stub 的区别？**
- Stub（打桩）：给依赖对象预设返回值，让被测代码能跑下去
- Mock（模拟对象）：既能打桩，又能**验证交互**（verify 调用了几次、传了什么参数）
- Mockito 里两者用同一个 `@Mock` 对象完成

**Q2：为什么要 verify？只断言返回值不够吗？**
- 不够。很多方法（如 `sendMail`）没有返回值或返回值不体现副作用。verify 能验证"副作用确实发生了"，如"确实发了信""确实删了验证码"。

**Q3：UnnecessaryStubbingException 是怎么产生的？怎么避免？**
- 严格模式下打桩未被使用就会抛。按需打桩，不要在所有用例统一打桩。见 2.4。

**Q4：@InjectMocks 是怎么注入的？**
- Mockito 按构造器/字段类型/名称匹配，把 `@Mock` 对象注入被测对象的依赖字段。

**Q5：单元测试中要不要连真实数据库/Redis？**
- 不要。那是集成测试。单元测试用 Mock 隔离外部依赖，保证快和稳定。

**Q6：什么情况下不该用 Mock？**
- 被测对象本身就是要测的集成逻辑（如 MyBatis Mapper 的 SQL 正确性），此时应连真实（或内存）数据库做集成测试。

---

## 第三部分：动手练习（单元小测）

> 建议自己写，写完运行 `mvn -pl bili-common test -Dtest=EmailServiceImplTest`。答案在讲义末。

### 练习 1（★）邮箱大小写与空格
`sendEmailCode` 内部把邮箱 `trim().toLowerCase()` 后再处理。请写一个测试：传入 `"  123456789@qq.com  "`（带前后空格），验证存验证码用的 key 是规范化后的邮箱。
提示：用 `verify(redisComponent).saveEmailCode(eq("123456789@qq.com"), anyString())`。

### 练习 2（★★）空验证码
写一个测试：调用 `verifyEmailCode(email, "")`，验证抛出 BaseException 且消息含"不能为空"，并确认**没有**调用 `redisComponent.getEmailCode`。

### 练习 3（★★）发信成功时不能误删验证码
在 `sendEmailCodeSuccess` 用例里加断言：验证 `redisComponent.cleanEmailCode(...)` **从未**被调用（正常发信不应清理验证码）。

### 练习 4（★★★）参数化测试 QQ 邮箱校验
用 `@ParameterizedTest` + `@ValueSource` 测一批邮箱：`12345@qq.com`（合法）、`1234@qq.com`（位数不足）、`abc@qq.com`（非数字）、`12345@qq.com.cn`（后缀不符），验证合法的不抛异常、非法的抛"只支持QQ邮箱"。
注意：为了避免真实发信，你需要 mock 掉整个链路。

### 练习 5（★★★）缺陷思考
`sendEmailCode` 里"发送限流 key"和"验证码"是两步独立操作，若 `saveEmailCode` 成功但 `setEmailCodeSendLimit` 因 Redis 抖动失败，会出现什么不一致？如何用事务/管道改进？（开放题，写进你的思考笔记，是很好的面试素材）

---

<details>
<summary>练习参考答案</summary>

**练习1：**
```java
@Test
void sendEmailCodeTrimsEmail() {
    stubMailSender();
    when(userInfoMapper.findByEmail("123456789@qq.com")).thenReturn(null);
    when(redisComponent.setEmailCodeSendLimit("123456789@qq.com")).thenReturn(true);
    emailService.sendEmailCode("  123456789@qq.com  ");
    verify(redisComponent).saveEmailCode(eq("123456789@qq.com"), anyString());
}
```

**练习2：**
```java
@Test
void verifyEmailCodeEmptyThrows() {
    BaseException ex = assertThrows(BaseException.class,
        () -> emailService.verifyEmailCode(QQ_EMAIL, ""));
    assertTrue(ex.getMsg().contains("不能为空"));
    verify(redisComponent, never()).getEmailCode(anyString());
}
```

**练习3：** 在 sendEmailCodeSuccess 末尾加：
```java
verify(redisComponent, never()).cleanEmailCode(anyString());
```

**练习4：** 关键是用 @ValueSource 提供邮箱数组，对每个非法邮箱 assertThrows；合法邮箱需 stub 完整链路（findByEmail→null, setLimit→true, stubMailSender）。注意严格模式下"合法邮箱"用例会用到所有桩，而"非法邮箱"用例不该打这些桩 —— 建议拆成两个测试方法，或给非法用例单独处理。

**练习5：** 要点：两步非原子，若限流设置失败则同一邮箱可绕过60秒限制反复发送（验证码被覆盖，用户收到多封邮件，最后一封有效）。改进：用 Redis 事务（ MULTI/EXEC）或 Lua 脚本保证原子性；或把限流判断与验证码写入合并为一个 Lua 操作。也可考虑先设限流再存码的顺序调整。
</details>

---

## 简历落点（更新）

> **bili-live 视频分享平台｜功能开发 + 测试体系建设**
> - 独立设计并实现邮箱验证码注册功能：基于 QQ SMTP 发送 6 位验证码，Redis 存储 + 60 秒限流 + 失败次数锁定，含前后端完整交互
> - 运用 Mockito 对 Service 层进行隔离单元测试，覆盖正常/异常/边界场景，验证交互行为（verify + ArgumentCaptor），共 9 个用例
> - 从零引入 JUnit5 + JaCoCo 测试体系，为核心算法编写单元测试，累计 38 个用例，覆盖率持续提升
