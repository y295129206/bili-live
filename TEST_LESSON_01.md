# 第一课时讲义：单元测试入门与算法测试实战

> 对应代码：
> - `bili-common/src/test/java/com/bililive/utils/SimilarityCalculatorTest.java`（18 用例）
> - `bili-common/src/test/java/com/bililive/utils/CollaborativeFilteringAlgorithmTest.java`（11 用例）
> - pom 改动：根 pom 移除 `skipTests`、加 JaCoCo + Surefire；common 加 `spring-boot-starter-test`
>
> 运行结果：29 个用例，28 通过 + 1 个 `@Disabled` 缺陷证据用例。

---

## 一、今天我们做了什么（先建立全局观）

```
真实代码(被测) ──被──> 测试代码 ──用──> JUnit5 框架 ──由──> Maven Surefire 执行 ──产出──> 测试报告 + JaCoCo 覆盖率
```

1. 移除 `skipTests=true` —— 之前项目所有测试都被跳过了，`skipTests` 是"测试开关的总闸"。
2. 引入 `spring-boot-starter-test` —— 一个"测试全家桶"依赖。
3. 用 JUnit 5 为两组纯算法写了 29 个测试用例。
4. 用一个测试**复现了真实缺陷**（冷启动阈值 50 vs 5）。

---

## 二、知识点深度讲解

### 2.1 为什么从"纯算法"开始？

单元测试的黄金法则是 **FIRST**：

| 原则 | 含义 | 本例体现 |
|---|---|---|
| **F**ast 快 | 毫秒级 | 29 个用例 0.5 秒跑完 |
| **I**ndependent 独立 | 用例间无依赖、无顺序要求 | 每个 @Test 自带数据 |
| **R**epeatable 可重复 | 任何环境结果一致 | 纯数学函数，无外部状态 |
| **S**elf-validating 自Validation | 自动断言，无需人工看 | `assertEquals` |
| **T**imely 及时 | 与代码同步 | 补测试时同步写 |

`SimilarityCalculator` 是纯静态数学函数——没有数据库、没有 Redis、没有 Spring 容器，是最理想的第一个靶子。**学习测试要从"无依赖"的代码入手，逐步过渡到有依赖的 Service/Controller。**

### 2.2 spring-boot-starter-test 里到底有什么？

这是一个 BOM 式的聚合依赖，传递引入（版本由 Spring Boot parent 锁定，无需自己写版本号）：

- **JUnit 5 (Jupiter)**：测试框架核心（`@Test`、`@BeforeEach`、`Assertions`）
- **Mockito**：Mock 框架（下一课重点）
- **AssertJ**：流式断言库（`assertThat(...).isEqualTo(...)`）
- **Hamcrest**：匹配器
- **Spring Test**：`MockMvc`、`@SpringBootTest`（集成测试用）
- **JSONassert / JsonPath**：JSON 断言

`scope=test` 的含义：**只在测试阶段生效，不会被打进生产 jar**。这是关键——测试代码和依赖绝不能污染生产产物。

### 2.3 JUnit 5 核心注解与断言（本例用到的）

**注解：**
- `@Test`：标记一个测试方法。JUnit 5 的测试方法**可以是包级私有**（不需要 `public`），这是和 JUnit 4 的区别。
- `@DisplayName`：给测试起一个可读的中文名，报告里直接显示，利于团队协作。
- `@ParameterizedTest` + `@CsvSource`：参数化测试，用数据表驱动，一行数据一个用例。
- `@Disabled`：跳过某个测试（本例用于"缺陷证据用例"）。
- `@Nested`：嵌套测试类，用于分组（本例导入了但你可以练习使用）。

**断言（都来自 `org.junit.jupiter.api.Assertions` 静态导入）：**
- `assertEquals(expected, actual, delta)` —— delta 是浮点误差容限，如 `1e-9`
- `assertFalse` / `assertTrue` —— 带 message 参数时，失败信息更清晰
- `assertAll(...)` —— 把多个断言打包，**全部执行**，一次性报告所有失败点（而不是第一个失败就中断）
- `assertThrows(...)` —— 断言抛异常（下一课练）

> 面试常问：`assertEquals` 比较两个对象时用的是什么？→ 用的是对象的 `equals()` 方法。比较浮点数为什么要用 delta？→ 浮点数有精度误差，直接比较 `==` 不可靠。

### 2.4 测试用例设计方法（本例实战）

这不是"随便写几个输入"，而是有章法的：

1. **等价类划分**：把输入分成若干类，每类取一个代表。
   - 余弦相似度：正常向量 / 零向量 / null / 长度不等 —— 4 个等价类
2. **边界值分析**：错误往往发生在边界。
   - 空数组 `new double[0]`、单元素数组、全 0 向量
3. **错误猜测**：基于经验猜测哪里容易错。
   - "除零" → 零向量测试；"null 解引用" → null 输入测试
4. **参数化**：一维向量的 4 组数据用 `@CsvSource` 一次覆盖。

### 2.5 "缺陷锁定"模式（本例最重要的工程思想）

发现缺陷后怎么办？我们有三种测试策略，本例全用上了：

1. **锁定当前行为**（`coldStartThresholdCurrentBehavior`，通过）：用测试固定"代码现在真实的样子"。好处：谁改了阈值，测试立刻报警，防止无意破坏。
2. **缺陷证据**（`coldStartThresholdShouldFollowDocumentation`，`@Disabled`）：按文档规格写，故意让它失败，然后 `@Disabled` 跳过并写明原因。它是"判决待定"的可执行记录。
3. **确认后收敛**：一旦确认阈值到底是 5 还是 50，就二选一（改代码 or 改文档），让"代码、测试、文档"三者一致，删除 `@Disabled`。

> 这套思路体现了成熟工程师的质量意识：**测试不只是"验证对不对"，更是"固化共识、暴露分歧"的工具。**

### 2.6 一个真实的教训：测试也会写错

`itemSimilarityJaccard` 第一次运行时失败了：`expected 0.4 but was 0.5`。

原因不是代码 bug，而是我算错了并集：`v1={u1,u2,u3}`、`v2={u1,u2,u4}`，交集 = 2，并集 = 3+3−2 = **4**，Jaccard = 2/4 = 0.5。我在注释里误写成"并集=5"。

**结论：测试失败后，第一步是判断"到底是代码错了，还是测试错了"，而不是急着改代码。** 这是新手最容易踩的坑。

---

## 三、面试可能的问题（附答题要点）

**Q1：什么是单元测试？它和集成测试、端到端测试的区别？**
- 单元：最小可测单元（方法/类），隔离外部依赖，快。
- 集成：多个模块组合，验证交互（如 Service + Mapper 连真实 DB）。
- E2E：从用户视角走完整流程（如 UI 自动化、接口全链路）。
- 用"测试金字塔"作答：底层单元测试最多最快，越往上越少越慢。

**Q2：`skipTests` 和 `maven.test.skip` 的区别？**
- `-DskipTests`：跳过测试**执行**，但测试代码仍会编译。
- `-Dmaven.test.skip=true`：连测试代码都**不编译**。
- 本项目原本在 pom 里写死 `skipTests=true`，等于默认永不跑测试——这是质量隐患。

**Q3：JUnit 4 和 JUnit 5 的区别？**
- JUnit 5 用 `@Test`（包路径 `org.junit.jupiter.api`），JUnit 4 是 `org.junit.Test`。
- JUnit 5 测试方法不必是 public；有 `@DisplayName`、参数化测试、断言改进等。

**Q4：`assertEquals` 比较浮点数为什么要传 delta？**
- 浮点运算有精度误差，`0.1+0.2 != 0.3`。delta 定义可接受的误差范围。

**Q5：发现一个 bug 但产品还没确认规格，你会怎么处理？**
- 用本例的"缺陷锁定 + @Disabled 证据用例"模式作答：固定当前行为 + 记录规格分歧 + 推动三方对齐，非常加分。

**Q6：什么是代码覆盖率？100% 覆盖率意味着没有 bug 吗？**
- 覆盖率是"测试执行到的代码比例"（行/分支/路径）。JaCoCo 报告在 `target/site/jacoco/index.html`。
- 100% 不等于无 bug：可能断言写错、可能没覆盖异常场景、可能逻辑本身错误。**覆盖率是必要不充分条件。**

**Q7：什么样的代码最难做单元测试？如何改善？**
- 强外部依赖、静态调用、new 对象、私有方法、全局状态。
- 改善：依赖注入（便于 Mock）、面向接口编程、拆分纯逻辑。可联系本项目"Controller 直连 Mapper 难测"的问题。

---

## 四、动手练习（单元小测 · 请你来完成）

> 建议：自己在 IDE 里新建测试类写，写完运行 `mvn -pl bili-common test -Dtest=你的类名`。
> 参考答案在下一节，**先自己做再看**。

### 练习 1（难度 ★）—— 余弦相似度参数化
仿照 `cosineSingleDimension`，用 `@CsvSource` 为**皮尔逊相关系数**写一个参数化测试，至少覆盖：完全正相关、完全负相关、无关（0）。提示：用简单数组如 `{1,2,3}`、`{2,4,6}`、`{3,2,1}`。

### 练习 2（难度 ★★）—— 异常断言
`normalizeWeights` 有一个潜在缺陷：如果 Map 里某个 value 是 `null`，`mapToDouble(Double::doubleValue)` 会抛 NPE。请写一个测试，用 `assertThrows(NullPointerException.class, ...)` 验证这个行为，把它作为"缺陷证据"。

### 练习 3（难度 ★★）—— ContentBasedAlgorithm 向量构建
新建 `ContentBasedAlgorithmTest`，测试 `buildVideoFeatureVector`：
- 传入 categoryId=1、pCategoryId=2、tags=`{"游戏"}`，返回的向量长度应为 150。
- 验证向量已归一化（所有元素平方和 ≈ 1）。

### 练习 4（难度 ★★★）—— 用测试发现隐藏 bug（面试黄金素材）
`buildVideoFeatureVector` 里对 tag 用了 `Math.abs(tag.hashCode())`。`Integer.MIN_VALUE` 的 `Math.abs` 仍是负数。请思考：一个 hash 恰好为 `Integer.MIN_VALUE` 的字符串会导致数组下标为负、抛 `ArrayIndexOutOfBoundsException`。虽然极难构造，但请写一个测试**论证这个风险**（可用反射或直接分析），并在注释里写出你的修复建议。

### 练习 5（难度 ★★★）—— 缺陷修复闭环
本项目冷启动阈值缺陷（文档50/代码5）：
1. 假设团队确认"文档正确，阈值应为50"，请说出你要改哪几个文件、改哪些测试。
2. 实际动手：把 `BEHAVIOR_THRESHOLD` 改成 50，更新测试，让所有测试通过。
3. 记录这个过程到你的简历话术里（STAR 法则）。

---

## 五、练习参考答案（做完再看）

<details>
<summary>点击展开</summary>

**练习1 参考：**
```java
@ParameterizedTest
@CsvSource({"1,2,3, 2,4,6, 1.0", "1,2,3, 3,2,1, -1.0"})
void pearsonParam(int a1,int a2,int a3, int b1,int b2,int b3, double expected){
    double[] a={a1,a2,a3}, b={b1,b2,b3};
    assertEquals(expected, SimilarityCalculator.calculatePearsonCorrelation(a,b),1e-9);
}
```
注意：`@CsvSource` 默认用逗号分隔，数组元素要拆成多个参数。

**练习2 参考：**
```java
@Test
void normalizeWithNullValueThrows(){
    Map<String,Double> m=new HashMap<>();
    m.put("a", null);
    assertThrows(NullPointerException.class, () -> SimilarityCalculator.normalizeWeights(m));
}
```

**练习3 参考：**
```java
@Test
void buildVectorShouldBeNormalized(){
    ContentBasedAlgorithm algo=new ContentBasedAlgorithm();
    double[] v=algo.buildVideoFeatureVector(1,2,Set.of("游戏"),"测试标题");
    assertEquals(150, v.length);
    double sum=0; for(double x:v) sum+=x*x;
    assertEquals(1.0, sum, 1e-9);
}
```

**练习4 要点：** `Math.abs(Integer.MIN_VALUE) == Integer.MIN_VALUE`（负数），`负数 % 100` 仍是负数 → 数组越界。修复建议：用 `Math.floorMod(hash, TAG_DIM)` 替代 `Math.abs(hash) % TAG_DIM`，floorMod 保证结果非负。

**练习5 要点：** 改 `CollaborativeFilteringAlgorithm.BEHAVIOR_THRESHOLD = 50`；删除 `@Disabled` 让证据用例通过；修改 `coldStartThresholdCurrentBehavior` 的断言（6条和4条现在都是新用户）；同步更新 `RECOMMENDATION_SYSTEM.md`（其实文档不用改了）。

</details>

---

## 六、本课时成果 → 简历落点

完成本课后，你可以在简历中写：

> **bili-live 视频分享平台｜测试体系建设（进行中）**
> - 从零引入 JUnit 5 + Mockito + JaCoCo 测试体系，移除 `skipTests` 配置，为核心推荐算法编写 29 个单元测试用例
> - 运用等价类划分、边界值分析、参数化测试等方法设计用例，覆盖正常/边界/异常场景
> - 通过单元测试发现"冷启动阈值文档承诺50、代码实际为5"的规格缺陷，建立"缺陷锁定 + 证据用例"的回归机制

下一课预告：**Mockito 打桩与 Service 层测试** —— 当被测代码依赖 Mapper、Redis 时，如何用 Mock 隔离外部依赖，让测试依然快速稳定。
