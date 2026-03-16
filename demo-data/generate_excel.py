"""生成SmartBI展示用的示例Excel数据文件"""
import pandas as pd
import numpy as np
import os
from datetime import datetime, timedelta

np.random.seed(42)
out_dir = os.path.dirname(os.path.abspath(__file__))

# ========== 1. 电商平台月度销售数据 ==========
months = pd.date_range('2025-01', periods=12, freq='MS').strftime('%Y-%m').tolist()
categories = ['手机数码', '服装鞋帽', '食品饮料', '家居家电', '美妆个护']
rows = []
for m in months:
    for cat in categories:
        base = {'手机数码': 580, '服装鞋帽': 420, '食品饮料': 310, '家居家电': 390, '美妆个护': 270}
        sales = int(base[cat] * np.random.uniform(0.8, 1.4) * (1 + months.index(m) * 0.02))
        revenue = round(sales * np.random.uniform(80, 350), 2)
        rows.append({'月份': m, '商品类目': cat, '销售量(件)': sales, '销售额(元)': revenue,
                     '退货率(%)': round(np.random.uniform(1.5, 8.0), 1)})
df1 = pd.DataFrame(rows)
df1.to_excel(os.path.join(out_dir, '电商平台月度销售数据.xlsx'), index=False, engine='openpyxl')
print('✓ 电商平台月度销售数据.xlsx (60行 × 5列)')

# ========== 2. 公司员工薪资与绩效分析 ==========
departments = ['技术部', '产品部', '市场部', '运营部', '人事部', '财务部']
levels = ['初级', '中级', '高级', '专家']
rows2 = []
for i in range(80):
    dept = np.random.choice(departments, p=[0.35, 0.15, 0.18, 0.15, 0.09, 0.08])
    level = np.random.choice(levels, p=[0.3, 0.35, 0.25, 0.1])
    base_salary = {'初级': 8000, '中级': 15000, '高级': 25000, '专家': 40000}
    salary = int(base_salary[level] * np.random.uniform(0.85, 1.25))
    perf = round(np.random.uniform(60, 100), 1)
    age = np.random.randint(22, 50)
    years = min(age - 21, np.random.randint(0, 15))
    gender = np.random.choice(['男', '女'])
    rows2.append({'员工编号': f'EMP{1001+i}', '部门': dept, '职级': level,
                  '性别': gender, '年龄': age, '工龄(年)': years,
                  '月薪(元)': salary, '绩效评分': perf})
df2 = pd.DataFrame(rows2)
df2.to_excel(os.path.join(out_dir, '公司员工薪资与绩效分析.xlsx'), index=False, engine='openpyxl')
print('✓ 公司员工薪资与绩效分析.xlsx (80行 × 8列)')

# ========== 3. 网站流量与用户行为数据 ==========
days = pd.date_range('2025-10-01', periods=90, freq='D')
rows3 = []
for d in days:
    weekday = d.weekday()
    base_pv = 12000 if weekday < 5 else 18000
    pv = int(base_pv * np.random.uniform(0.8, 1.3))
    uv = int(pv * np.random.uniform(0.3, 0.5))
    new_users = int(uv * np.random.uniform(0.15, 0.35))
    bounce = round(np.random.uniform(30, 65), 1)
    avg_duration = round(np.random.uniform(2.5, 8.0), 1)
    orders = int(uv * np.random.uniform(0.02, 0.06))
    rows3.append({'日期': d.strftime('%Y-%m-%d'), '页面浏览量(PV)': pv,
                  '独立访客数(UV)': uv, '新用户数': new_users,
                  '跳出率(%)': bounce, '平均访问时长(分钟)': avg_duration,
                  '下单数': orders})
df3 = pd.DataFrame(rows3)
df3.to_excel(os.path.join(out_dir, '网站流量与用户行为数据.xlsx'), index=False, engine='openpyxl')
print('✓ 网站流量与用户行为数据.xlsx (90行 × 7列)')

# ========== 4. 各城市空气质量指数(AQI) ==========
cities = ['北京', '上海', '广州', '深圳', '成都', '杭州', '武汉', '西安', '南京', '重庆']
months_q = pd.date_range('2025-01', periods=12, freq='MS').strftime('%Y-%m').tolist()
rows4 = []
base_aqi = {'北京': 95, '上海': 72, '广州': 58, '深圳': 45, '成都': 82,
            '杭州': 62, '武汉': 78, '西安': 98, '南京': 68, '重庆': 70}
for m in months_q:
    mi = months_q.index(m)
    season_factor = 1.0 + 0.3 * np.cos((mi - 0) * np.pi / 6)  # 冬高夏低
    for city in cities:
        aqi = int(base_aqi[city] * season_factor * np.random.uniform(0.8, 1.2))
        pm25 = round(aqi * np.random.uniform(0.5, 0.8), 1)
        pm10 = round(aqi * np.random.uniform(0.8, 1.2), 1)
        so2 = round(np.random.uniform(5, 30), 1)
        level = '优' if aqi <= 50 else ('良' if aqi <= 100 else ('轻度污染' if aqi <= 150 else '中度污染'))
        rows4.append({'月份': m, '城市': city, 'AQI指数': aqi, 'PM2.5(μg/m³)': pm25,
                      'PM10(μg/m³)': pm10, 'SO2(μg/m³)': so2, '空气质量等级': level})
df4 = pd.DataFrame(rows4)
df4.to_excel(os.path.join(out_dir, '各城市空气质量指数AQI.xlsx'), index=False, engine='openpyxl')
print('✓ 各城市空气质量指数AQI.xlsx (120行 × 7列)')

# ========== 5. 学生成绩分析数据 ==========
subjects = ['语文', '数学', '英语', '物理', '化学']
classes = ['高三(1)班', '高三(2)班', '高三(3)班']
rows5 = []
for cls in classes:
    for i in range(40):
        student_id = f'{classes.index(cls)+1}{i+1:02d}'
        name = f'学生{student_id}'
        gender = np.random.choice(['男', '女'])
        scores = {}
        for sub in subjects:
            mu = {'语文': 92, '数学': 85, '英语': 88, '物理': 78, '化学': 80}
            scores[sub] = min(150 if sub in ['语文', '数学', '英语'] else 100,
                             max(30, int(np.random.normal(mu[sub], 15))))
        total = sum(scores.values())
        row = {'班级': cls, '学号': student_id, '姓名': name, '性别': gender}
        row.update(scores)
        row['总分'] = total
        rows5.append(row)
df5 = pd.DataFrame(rows5)
df5.to_excel(os.path.join(out_dir, '学生成绩分析数据.xlsx'), index=False, engine='openpyxl')
print('✓ 学生成绩分析数据.xlsx (120行 × 10列)')

print('\n全部生成完毕！文件位于:', out_dir)
