import React, { useEffect, useState } from 'react';
import {
  LineChart,
  Line,
  BarChart,
  Bar,
  XAxis,
  YAxis,
  CartesianGrid,
  Tooltip,
  Legend,
  ResponsiveContainer,
  Label
} from 'recharts';
import axios from 'axios';
import './EnergyChart.css';

interface EnergyData {
  hour: string;
  totalConsumptionKwh: number;
}

interface EnergyChartProps {
  deviceId: string;
  deviceName: string;
}

export const EnergyChart: React.FC<EnergyChartProps> = ({ deviceId, deviceName }) => {
  const [data, setData] = useState<EnergyData[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [selectedDate, setSelectedDate] = useState(new Date().toISOString().split('T')[0]);
  const [chartType, setChartType] = useState<'line' | 'bar'>('line');
  const [totalConsumption, setTotalConsumption] = useState(0);

  const fetchData = async (date: string) => {
    try {
      setLoading(true);
      setError(null);

      const token = localStorage.getItem('token');
      if (!token) {
        setError('No authentication token found');
        return;
      }

      // Fetch hourly totals
      const response = await axios.get(
        `http://localhost:8080/api/monitoring/hourly-totals/${deviceId}?date=${date}`,
        {
          headers: {
            'Authorization': `Bearer ${token}`
          }
        }
      );

      // Handle both array response (old) and object response with data field (new)
      const hourlyData = Array.isArray(response.data) ? response.data : response.data?.data || [];
      
      if (hourlyData && hourlyData.length > 0) {
        // Format the data for the chart
        const formattedData = hourlyData.map((item: any) => ({
          hour: new Date(item.hour).toLocaleTimeString('en-US', { 
            hour: '2-digit', 
            minute: '2-digit',
            hour12: false 
          }),
          totalConsumptionKwh: parseFloat(item.totalConsumptionKwh.toFixed(2)),
          fullTime: item.hour
        }));

        setData(formattedData);

        // Calculate total consumption for the day
        const total = formattedData.reduce(
          (sum: number, item: any) => sum + item.totalConsumptionKwh,
          0
        );
        setTotalConsumption(parseFloat(total.toFixed(2)));
      }
    } catch (err) {
      console.error('Error fetching energy data:', err);
      setError('Failed to load energy data. Please try again.');
      setData([]);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchData(selectedDate);
  }, [deviceId, selectedDate]);

  const handleDateChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    setSelectedDate(e.target.value);
  };

  return (
    <div className="energy-chart-container">
      <div className="energy-chart-header">
        <div className="chart-title">
          <h3>⚡ Energy Consumption Chart</h3>
          <p className="device-name">{deviceName}</p>
        </div>
        
        <div className="chart-controls">
          <div className="date-selector">
            <label htmlFor="date-input">📅 Select Date:</label>
            <input
              id="date-input"
              type="date"
              value={selectedDate}
              onChange={handleDateChange}
              className="date-input"
            />
          </div>

          <div className="chart-type-selector">
            <label>📊 Chart Type:</label>
            <button
              className={`type-btn ${chartType === 'line' ? 'active' : ''}`}
              onClick={() => setChartType('line')}
            >
              📈 Line
            </button>
            <button
              className={`type-btn ${chartType === 'bar' ? 'active' : ''}`}
              onClick={() => setChartType('bar')}
            >
              📊 Bar
            </button>
          </div>
        </div>
      </div>

      <div className="energy-stats">
        <div className="stat-item">
          <span className="stat-label">Total Daily Consumption:</span>
          <span className="stat-value">{totalConsumption} kWh</span>
        </div>
        <div className="stat-item">
          <span className="stat-label">Average Hourly:</span>
          <span className="stat-value">
            {data.length > 0 ? (totalConsumption / data.length).toFixed(2) : '0'} kWh
          </span>
        </div>
        <div className="stat-item">
          <span className="stat-label">Data Points:</span>
          <span className="stat-value">{data.length}</span>
        </div>
      </div>

      {loading && (
        <div className="loading-state">
          <div className="spinner"></div>
          <p>Loading energy data...</p>
        </div>
      )}

      {error && (
        <div className="error-state">
          <p>⚠️ {error}</p>
          <button onClick={() => fetchData(selectedDate)} className="retry-btn">
            Retry
          </button>
        </div>
      )}

      {!loading && !error && data.length > 0 && (
        <div className="chart-wrapper">
          <ResponsiveContainer width="100%" height={400}>
            {chartType === 'line' ? (
              <LineChart data={data} margin={{ top: 5, right: 30, left: 0, bottom: 5 }}>
                <CartesianGrid strokeDasharray="3 3" />
                <XAxis 
                  dataKey="hour" 
                  angle={-45}
                  textAnchor="end"
                  height={80}
                  tick={{ fontSize: 12 }}
                />
                <YAxis 
                  label={{ value: 'Consumption (kWh)', angle: -90, position: 'insideLeft' }}
                />
                <Tooltip 
                  formatter={(value) => value.toFixed(2)}
                  labelFormatter={(label) => `Time: ${label}`}
                  contentStyle={{
                    backgroundColor: '#f5f5f5',
                    border: '1px solid #ccc',
                    borderRadius: '4px'
                  }}
                />
                <Legend />
                <Line
                  type="monotone"
                  dataKey="totalConsumptionKwh"
                  stroke="#3498db"
                  dot={{ fill: '#3498db', r: 4 }}
                  activeDot={{ r: 6 }}
                  name="Hourly Consumption"
                  strokeWidth={2}
                  isAnimationActive={true}
                />
              </LineChart>
            ) : (
              <BarChart data={data} margin={{ top: 5, right: 30, left: 0, bottom: 5 }}>
                <CartesianGrid strokeDasharray="3 3" />
                <XAxis 
                  dataKey="hour"
                  angle={-45}
                  textAnchor="end"
                  height={80}
                  tick={{ fontSize: 12 }}
                />
                <YAxis 
                  label={{ value: 'Consumption (kWh)', angle: -90, position: 'insideLeft' }}
                />
                <Tooltip 
                  formatter={(value) => value.toFixed(2)}
                  labelFormatter={(label) => `Time: ${label}`}
                  contentStyle={{
                    backgroundColor: '#f5f5f5',
                    border: '1px solid #ccc',
                    borderRadius: '4px'
                  }}
                />
                <Legend />
                <Bar
                  dataKey="totalConsumptionKwh"
                  fill="#2ecc71"
                  name="Hourly Consumption"
                  radius={[8, 8, 0, 0]}
                  isAnimationActive={true}
                />
              </BarChart>
            )}
          </ResponsiveContainer>
        </div>
      )}

      {!loading && !error && data.length === 0 && (
        <div className="empty-state">
          <p>📊 No data available for {selectedDate}</p>
          <p className="hint">Data will appear after the device generates readings</p>
        </div>
      )}
    </div>
  );
};

export default EnergyChart;

